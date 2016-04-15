package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.compiler.gen.CodeVisitor;
import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.compiler.gen.PrototypeContext;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.compiler.gen.block.LuaBinaryOperation;
import net.sandius.rembulan.compiler.gen.block.LuaInstruction;
import net.sandius.rembulan.compiler.gen.block.StaticMathImplementation;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.RawOperators;
import net.sandius.rembulan.core.Resumable;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.core.impl.DefaultSavedState;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class CodeEmitter {

	public final int LV_STATE = 1;
	public final int LV_OBJECTSINK = 2;
	public final int LV_RESUME = 3;
	public final int LV_VARARGS = 4;  // index of the varargs argument, if present

	private final ClassEmitter parent;
	private final PrototypeContext context;

	private final int numOfParameters;
	private final boolean isVararg;

	private final MethodNode invokeMethodNode;
	private final MethodNode resumeMethodNode;
	private final MethodNode runMethodNode;
	private MethodNode saveStateNode;

	private final Map<Object, LabelNode> labels;
	private final ArrayList<LabelNode> resumptionPoints;

	private final InsnList resumeSwitch;
	private final InsnList code;
	private final InsnList errorState;
	private final InsnList resumeHandler;

	public CodeEmitter(ClassEmitter parent, PrototypeContext context, int numOfParameters, boolean isVararg) {
		this.parent = Check.notNull(parent);
		this.context = Check.notNull(context);
		this.labels = new HashMap<>();
		this.resumptionPoints = new ArrayList<>();

		this.numOfParameters = numOfParameters;
		this.isVararg = isVararg;

		this.invokeMethodNode = new MethodNode(
				ACC_PUBLIC,
				"invoke",
				invokeMethodType().getDescriptor(),
				null,
				exceptions());

		this.resumeMethodNode = new MethodNode(
				ACC_PUBLIC,
				"resume",
				Type.getMethodType(
						Type.VOID_TYPE,
						Type.getType(LuaState.class),
						Type.getType(ObjectSink.class),
						Type.getType(Serializable.class)).getDescriptor(),
						null,
				exceptions());

		this.runMethodNode = new MethodNode(
				ACC_PRIVATE,
				runMethodName(),
				runMethodType().getDescriptor(),
				null,
				exceptions());

		this.saveStateNode = null;

		resumeSwitch = new InsnList();
		code = new InsnList();
		errorState = new InsnList();
		resumeHandler = new InsnList();
	}

	private int registerOffset() {
		return isVararg ? LV_VARARGS + 1 : LV_VARARGS;
	}

	public PrototypeContext context() {
		return context;
	}

	public MethodNode invokeMethodNode() {
		return invokeMethodNode;
	}

	public MethodNode resumeMethodNode() {
		return resumeMethodNode;
	}

	public MethodNode runMethodNode() {
		return runMethodNode;
	}

	public MethodNode saveNode() {
		return saveStateNode;
	}

	private String runMethodName() {
		return "run";
	}

	private Type runMethodType() {
		ArrayList<Type> args = new ArrayList<>();

		args.add(Type.getType(LuaState.class));
		args.add(Type.getType(ObjectSink.class));
		args.add(Type.INT_TYPE);
		if (isVararg) {
			args.add(ASMUtils.arrayTypeFor(Object.class));
		}
		for (int i = 0; i < numOfRegisters(); i++) {
			args.add(Type.getType(Object.class));
		}
		return Type.getMethodType(Type.VOID_TYPE, args.toArray(new Type[0]));
	}

	private Type invokeMethodType() {
		return parent.invokeMethodType();
	}

	private String[] exceptions() {
		return new String[] { Type.getInternalName(ControlThrowable.class) };
	}

	protected LabelNode _l(Object key) {
		LabelNode l = labels.get(key);

		if (l != null) {
			return l;
		}
		else {
			LabelNode nl = new LabelNode();
			labels.put(key, nl);
			return nl;
		}
	}

	public void _ignored(Object o) {
		System.out.println("// Ignored: " + o.getClass() + ": " + o.toString());
	}

	public void _missing(Object o) {
		throw new UnsupportedOperationException("Not implemented: " + o.getClass() + ": " + o.toString());
	}

	public InsnList code() {
		return code;
	}

	public AbstractInsnNode loadThis() {
		return new VarInsnNode(ALOAD, 0);
	}

	public static AbstractInsnNode loadNull() {
		return new InsnNode(ACONST_NULL);
	}

	public static InsnList loadBoxedConstant(Object k, Class<?> castTo) {
		InsnList il = new InsnList();

		if (k == null) {
			il.add(loadNull());
		}
		else if (k instanceof Boolean) {
			il.add(ASMUtils.loadBoxedBoolean((Boolean) k));
		}
		else if (k instanceof Double || k instanceof Float) {
			il.add(ASMUtils.loadDouble(((Number) k).doubleValue()));
			il.add(ASMUtils.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
		}
		else if (k instanceof Number) {
			il.add(ASMUtils.loadLong(((Number) k).longValue()));
			il.add(ASMUtils.box(Type.LONG_TYPE, Type.getType(Long.class)));
		}
		else if (k instanceof String) {
			il.add(new LdcInsnNode((String) k));
		}
		else {
			throw new UnsupportedOperationException("Illegal constant type: " + k.getClass());
		}

		if (castTo != null) {
			Check.notNull(k);
			if (!castTo.isAssignableFrom(k.getClass())) {
				il.add(checkCast(castTo));
			}
		}

		return il;
	}

	public static InsnList loadBoxedConstant(Object k) {
		return loadBoxedConstant(k, null);
	}

	public InsnList loadConstant(int idx, Class castTo) {
		return loadBoxedConstant(context.getConst(idx), castTo);
	}

	public InsnList loadConstant(int idx) {
		return loadConstant(idx, null);
	}

	public AbstractInsnNode loadRegisterValue(int registerIndex) {
		return new VarInsnNode(ALOAD, registerOffset() + registerIndex);
	}

	public InsnList loadRegisterValue(int registerIndex, Class castTo) {
		InsnList il = new InsnList();
		il.add(loadRegisterValue(registerIndex));
		il.add(checkCast(castTo));
		return il;
	}

	public InsnList loadRegister(int registerIndex, SlotState slots, Class<?> castTo) {
		Check.notNull(slots);
		Check.nonNegative(registerIndex);

		InsnList il = new InsnList();

		if (slots.isCaptured(registerIndex)) {
			il.add(getDownvalue(registerIndex));
			il.add(getUpvalueValue());
		}
		else {
			il.add(loadRegisterValue(registerIndex));
		}

		Class clazz = Object.class;

		if (castTo != null && !castTo.isAssignableFrom(clazz)) {
			il.add(checkCast(castTo));
		}

		return il;
	}

	public InsnList loadRegister(int registerIndex, SlotState slots) {
		return loadRegister(registerIndex, slots, null);
	}

	public InsnList loadRegisters(int firstRegisterIndex, SlotState slots, int num) {
		InsnList il = new InsnList();
		for (int i = 0; i < num; i++) {
			il.add(loadRegister(firstRegisterIndex + i, slots));
		}
		return il;
	}

	public InsnList packRegistersIntoArray(int firstRegisterIndex, SlotState slots, int num) {
		InsnList il = new InsnList();

		il.add(ASMUtils.loadInt(num));
		il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
		for (int i = 0; i < num; i++) {
			il.add(new InsnNode(DUP));
			il.add(ASMUtils.loadInt(i));
			il.add(loadRegister(firstRegisterIndex + i, slots));
			il.add(new InsnNode(AASTORE));
		}

		return il;
	}

	public InsnList packRegistersUpToStackTopIntoArray(int firstRegisterIndex, SlotState slots) {
		Check.isTrue(slots.hasVarargs());

		InsnList il = new InsnList();

		int n = slots.varargPosition() - firstRegisterIndex;

		if (n == 0) {
			// just take the varargs
			il.add(loadObjectSink());
			il.add(ObjectSinkMethods.toArray());
		}
		else if (n < 0) {
			// drop n elements from the object sink
			il.add(loadObjectSink());
			il.add(new InsnNode(DUP));
			il.add(ASMUtils.loadInt(-n));
			il.add(ObjectSinkMethods.drop());
			il.add(ObjectSinkMethods.toArray());
		}
		else {
			// prepend n elements
			il.add(packRegistersIntoArray(firstRegisterIndex, slots, n));
			il.add(loadObjectSink());
			il.add(ObjectSinkMethods.toArray());
			il.add(UtilMethods.concatenateArrays());
		}

		return il;
	}


	// FIXME: come up with a better name
	public InsnList mapInvokeArgumentsToKinds(int fromIndex, SlotState slots, int desired, int actual) {
		if (desired > 0) {
			if (actual == 0) {
				return packRegistersIntoArray(fromIndex, slots, desired - 1);  // passing args through an array
			}
			else {
				Check.isEq(desired, actual);
				return loadRegisters(fromIndex, slots, desired - 1);  // passing args through the stack
			}
		}
		else {
			Check.isEq(desired, actual);
			return packRegistersUpToStackTopIntoArray(fromIndex, slots);
		}
	}


	public InsnList getDownvalue(int idx) {
		InsnList il = new InsnList();
		il.add(new VarInsnNode(ALOAD, registerOffset() + idx));
		il.add(checkCast(Upvalue.class));
		return il;
	}

	public InsnList loadRegisterOrConstant(int rk, SlotState slots, Class castTo) {
		Check.notNull(slots);

		if (rk < 0) {
			// it's a constant
			return loadConstant(-rk - 1, castTo);
		}
		else {
			return loadRegister(rk, slots, castTo);
		}
	}

	public InsnList loadRegisterOrConstant(int rk, SlotState slots) {
		return loadRegisterOrConstant(rk, slots, null);
	}

	private static AbstractInsnNode loadUnboxedConstant(Object o, Type requiredType) {
		if (o instanceof Number) {

			Number n = (Number) o;

			if (n instanceof Double || n instanceof Float) {

				double d = n.doubleValue();

				if (requiredType.equals(Type.LONG_TYPE)) {
					// FIXME: which one is better?
					return ASMUtils.loadLong(n.longValue());
//					return ASMUtils.loadLong((long) d);
				}
				else if (requiredType.equals(Type.INT_TYPE)) {
					// FIXME: which one is better?
					return ASMUtils.loadInt(n.intValue());
//					return ASMUtils.loadLong((int) d);
				}
				else if (requiredType.equals(Type.DOUBLE_TYPE)) {
					// FIXME: which one is better?
					return ASMUtils.loadDouble(n.doubleValue());
//					return ASMUtils.loadDouble(d);
				}
				else {
					throw new UnsupportedOperationException("Unsupported required type: " + requiredType);
				}
			}
			else {
				long l = n.longValue();

				if (requiredType.equals(Type.LONG_TYPE)) {
					// FIXME: which one is better?
					return ASMUtils.loadLong(n.longValue());
//					return ASMUtils.loadLong(l);
				}
				else if (requiredType.equals(Type.INT_TYPE)) {
					// FIXME: which one is better?
					return ASMUtils.loadInt(n.intValue());
//					return ASMUtils.loadLong((int) l);
				}
				else if (requiredType.equals(Type.DOUBLE_TYPE)) {
					// FIXME: which one is better?
					return ASMUtils.loadDouble(n.doubleValue());
//					return ASMUtils.loadDouble((double) l);
				}
				else {
					throw new UnsupportedOperationException("Unsupported required type: " + requiredType);
				}
			}
		}
		else {
			throw new UnsupportedOperationException("Unsupported constant type: "
					+ (o != null ? o.getClass().getName() : "null"));
		}
	}

	public static AbstractInsnNode unbox(Class clazz, Type requiredType) {
		if (requiredType.equals(Type.LONG_TYPE)) {
			return longValue(clazz);
		}
		else if (requiredType.equals(Type.INT_TYPE)) {
			return intValue(clazz);
		}
		else if (requiredType.equals(Type.DOUBLE_TYPE)) {
			return doubleValue(clazz);
		}
		else {
			throw new UnsupportedOperationException("Unsupported required type: " + requiredType);
		}
	}

	public InsnList loadUnboxedRegisterOrConstant(int rk, SlotState slots, Type requiredType) {
		InsnList il = new InsnList();

		// FIXME: this duplicates the retrieval code!
		if (rk < 0) {
			// it's a constant
			Object c = context.getConst(-rk - 1);
			il.add(loadUnboxedConstant(c, requiredType));
		}
		else {
			// it's a register
			il.add(loadRegister(rk, slots, Number.class));
			il.add(unbox(Number.class, requiredType));
		}

		return il;
	}

	private AbstractInsnNode storeRegisterValue(int registerIndex) {
		return new VarInsnNode(ASTORE, registerOffset() + registerIndex);
	}

	public InsnList storeToRegister(int registerIndex, SlotState slots) {
		InsnList il = new InsnList();

		if (slots.isCaptured(registerIndex)) {
			il.add(getDownvalue(registerIndex));
			il.add(new InsnNode(SWAP));
			il.add(setUpvalueValue());
		}
		else {
			il.add(storeRegisterValue(registerIndex));
		}

		return il;
	}

	public AbstractInsnNode dispatchGeneric(String methodName, int numArgs) {
		ArrayList<Type> args = new ArrayList<>();
		args.add(Type.getType(LuaState.class));
		args.add(Type.getType(ObjectSink.class));
		for (int i = 0; i < numArgs; i++) {
			args.add(Type.getType(Object.class));
		}
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				methodName,
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						args.toArray(new Type[0])),
				false);
	}

	public AbstractInsnNode dispatchNumeric(String methodName, int numArgs) {
		Type[] args = new Type[numArgs];
		Arrays.fill(args, Type.getType(Number.class));
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				methodName,
				Type.getMethodDescriptor(
						Type.getType(Number.class),
						args),
				false);
	}

	public AbstractInsnNode dispatchIndex() {
		return dispatchGeneric("index", 2);
	}

	public AbstractInsnNode dispatchNewindex() {
		return dispatchGeneric("newindex", 3);
	}

	public AbstractInsnNode dispatchCall(int kind) {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				"call",
				InvokeKind.staticMethodType(kind).getDescriptor(),
				false);
	}

	public AbstractInsnNode dispatchContinueLoop() {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				"continueLoop",
				Type.getMethodDescriptor(
						Type.BOOLEAN_TYPE,
						Type.getType(Number.class),
						Type.getType(Number.class),
						Type.getType(Number.class)),
				false);
	}

	public static AbstractInsnNode checkCast(Class clazz) {
		return new TypeInsnNode(CHECKCAST, Type.getInternalName(clazz));
	}

	public AbstractInsnNode loadLuaState() {
		return new VarInsnNode(ALOAD, LV_STATE);
	}

	public AbstractInsnNode loadObjectSink() {
		return new VarInsnNode(ALOAD, LV_OBJECTSINK);
	}

	public InsnList loadDispatchPreamble() {
		InsnList il = new InsnList();
		il.add(loadLuaState());
		il.add(loadObjectSink());
		return il;
	}

	public InsnList retrieve_0() {
		InsnList il = new InsnList();

		il.add(loadObjectSink());
		il.add(ObjectSinkMethods.get(0));

		return il;
	}

	public InsnList retrieveAndStore(int firstIdx, SlotState slots, int num) {
		InsnList il = new InsnList();
		if (num > 0) {
			il.add(loadObjectSink());
			for (int i = 0; i < num; i++) {
				if (i + 1 < num) {
					il.add(new InsnNode(DUP));
				}
				il.add(ObjectSinkMethods.get(i));
				il.add(storeToRegister(firstIdx + i, slots));
			}
		}
		return il;
	}

	@Deprecated
	public void _save_pc(Object o) {
		LabelNode rl = _l(o);

		int idx = resumptionPoints.size();
		resumptionPoints.add(rl);

		code.add(ASMUtils.loadInt(idx));
		code.add(new VarInsnNode(ISTORE, LV_RESUME));
	}

	@Deprecated
	public void _resumptionPoint(Object label) {
		_label_here(label);
	}

	private LabelNode l_insns_begin;
	private LabelNode l_body_begin;
	private LabelNode l_error_state;

	private LabelNode l_handler_begin;

	public void begin() {
		l_insns_begin = new LabelNode();
		runMethodNode.instructions.add(l_insns_begin);

		l_body_begin = new LabelNode();
		l_error_state = new LabelNode();

		l_handler_begin = new LabelNode();

		resumptionPoints.add(l_body_begin);

		code.add(l_body_begin);
		_frame_same(code);

		// FIXME: the initial Target emits a label + stack frame immediately after this;
		// that is illegal if there is no instruction in between
		code.add(new InsnNode(NOP));
	}

	public void end() {
		if (isResumable()) {
			_error_state();
		}
		_dispatch_table();
		if (isResumable()) {
			_resumption_handler();
		}

		// local variable declaration

		LabelNode l_insns_end = new LabelNode();

		List<LocalVariableNode> locals = runMethodNode.localVariables;
		locals.add(new LocalVariableNode("this", parent.thisClassType().getDescriptor(), null, l_insns_begin, l_insns_end, 0));
		locals.add(new LocalVariableNode("state", Type.getDescriptor(LuaState.class), null, l_insns_begin, l_insns_end, LV_STATE));
		locals.add(new LocalVariableNode("sink", Type.getDescriptor(ObjectSink.class), null, l_insns_begin, l_insns_end, LV_OBJECTSINK));
		locals.add(new LocalVariableNode("rp", Type.INT_TYPE.getDescriptor(), null, l_insns_begin, l_insns_end, LV_RESUME));

		if (isVararg) {
			locals.add(new LocalVariableNode(
					"varargs",
					ASMUtils.arrayTypeFor(Object.class).getDescriptor(),
					null,
					l_insns_begin,
					l_insns_end,
					LV_VARARGS
					));
		}

		for (int i = 0; i < numOfRegisters(); i++) {
			locals.add(new LocalVariableNode("r_" + i, Type.getDescriptor(Object.class), null, l_insns_begin, l_insns_end, registerOffset() + i));
		}

//		if (isResumable()) {
//			locals.add(new LocalVariableNode("ct", Type.getDescriptor(ControlThrowable.class), null, l_handler_begin, l_handler_end, registerOffset() + numOfRegisters()));
//		}

		// TODO: check these
//		runMethodNode.maxLocals = numOfRegisters() + 4;
//		runMethodNode.maxStack = numOfRegisters() + 5;

		runMethodNode.maxLocals = locals.size();
		runMethodNode.maxStack = 4 + numOfRegisters() + 5;

		runMethodNode.instructions.add(resumeSwitch);
		runMethodNode.instructions.add(code);
		runMethodNode.instructions.add(errorState);
		runMethodNode.instructions.add(resumeHandler);

		runMethodNode.instructions.add(l_insns_end);

		emitInvokeNode();
		emitResumeNode();

		MethodNode save = saveStateNode();
		if (save != null) {
			parent.node().methods.add(save);
		}
	}

	private void emitInvokeNode() {
		InsnList il = invokeMethodNode.instructions;
		List<LocalVariableNode> locals = invokeMethodNode.localVariables;

		LabelNode begin = new LabelNode();
		LabelNode end = new LabelNode();

		int invokeKind = InvokeKind.encode(numOfParameters, isVararg);


		il.add(begin);

		il.add(new VarInsnNode(ALOAD, 0));  // this
		il.add(new VarInsnNode(ALOAD, 1));  // state
		il.add(new VarInsnNode(ALOAD, 2));  // sink
		il.add(ASMUtils.loadInt(0));  // resumption point

		if (invokeKind > 0) {
			// we have (invokeKind - 1) standalone parameters, mapping them onto #numOfRegisters

			for (int i = 0; i < numOfRegisters(); i++) {
				if (i < invokeKind - 1) {
					il.add(new VarInsnNode(ALOAD, 3 + i));
				}
				else {
					il.add(new InsnNode(ACONST_NULL));
				}
			}
		}
		else {
			// variable number of parameters, encoded in an array at position 3

			if (isVararg) {
				il.add(new VarInsnNode(ALOAD, 3));
				il.add(UtilMethods.arrayFrom(numOfParameters));
			}

			// load #numOfParameters, mapping them onto #numOfRegisters

			for (int i = 0; i < numOfRegisters(); i++) {
				if (i < numOfParameters) {
					il.add(new VarInsnNode(ALOAD, 3));  // TODO: use dup instead?
					il.add(UtilMethods.getArrayElementOrNull(i));
				}
				else {
					il.add(new InsnNode(ACONST_NULL));
				}
			}

		}

		il.add(new MethodInsnNode(
				INVOKESPECIAL,
				parent.thisClassType().getInternalName(),
				runMethodName(),
				runMethodType().getDescriptor(),
				false));

		il.add(new InsnNode(RETURN));
		il.add(end);

		locals.add(new LocalVariableNode("this", parent.thisClassType().getDescriptor(), null, begin, end, 0));
		locals.add(new LocalVariableNode("state", Type.getDescriptor(LuaState.class), null, begin, end, 1));
		locals.add(new LocalVariableNode("sink", Type.getDescriptor(ObjectSink.class), null, begin, end, 2));
		if (invokeKind < 0) {
			locals.add(new LocalVariableNode("args", ASMUtils.arrayTypeFor(Object.class).getDescriptor(), null, begin, end, 3));

			// TODO: maxLocals, maxStack
		}
		else {
			for (int i = 0; i < invokeKind; i++) {
				locals.add(new LocalVariableNode("arg_" + i, Type.getDescriptor(Object.class), null, begin, end, 3 + i));
			}

			invokeMethodNode.maxLocals = 3 + invokeKind;
			invokeMethodNode.maxStack = 4 + numOfRegisters();
		}
	}

	private void emitResumeNode() {
		if (isResumable()) {
			InsnList il = resumeMethodNode.instructions;
			List<LocalVariableNode> locals = resumeMethodNode.localVariables;

			LabelNode begin = new LabelNode();
			LabelNode vars = new LabelNode();
			LabelNode end = new LabelNode();

			il.add(begin);

			il.add(new VarInsnNode(ALOAD, 3));
			il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(DefaultSavedState.class)));

			il.add(vars);

			il.add(new VarInsnNode(ASTORE, 4));

			il.add(new VarInsnNode(ALOAD, 0));  // this
			il.add(new VarInsnNode(ALOAD, 1));  // state
			il.add(new VarInsnNode(ALOAD, 2));  // sink

			il.add(new VarInsnNode(ALOAD, 4));  // saved state
			il.add(new MethodInsnNode(
					INVOKEVIRTUAL,
					Type.getInternalName(DefaultSavedState.class),
					"resumptionPoint",
					Type.getMethodDescriptor(
							Type.INT_TYPE),
					false
			));  // resumption point

			if (isVararg) {
				il.add(new VarInsnNode(ALOAD, 4));
				il.add(new MethodInsnNode(
						INVOKEVIRTUAL,
						Type.getInternalName(DefaultSavedState.class),
						"varargs",
						Type.getMethodDescriptor(
								ASMUtils.arrayTypeFor(Object.class)),
						false
				));
			}

			// registers
			if (numOfRegisters() > 0) {
				il.add(new VarInsnNode(ALOAD, 4));
				il.add(new MethodInsnNode(
						INVOKEVIRTUAL,
						Type.getInternalName(DefaultSavedState.class),
						"registers",
						Type.getMethodDescriptor(
								ASMUtils.arrayTypeFor(Object.class)),
						false
				));

				for (int i = 0; i < numOfRegisters(); i++) {

					// Note: it might be more elegant to use a local variable
					// to store the array instead of having to perform SWAPs

					if (i + 1 < numOfRegisters()) {
						il.add(new InsnNode(DUP));
					}
					il.add(ASMUtils.loadInt(i));
					il.add(new InsnNode(AALOAD));
					if (i + 1 < numOfRegisters()) {
						il.add(new InsnNode(SWAP));
					}
				}
			}

			// call run(...)
			il.add(new MethodInsnNode(
					INVOKESPECIAL,
					parent.thisClassType().getInternalName(),
					runMethodName(),
					runMethodType().getDescriptor(),
					false
			));

			il.add(new InsnNode(RETURN));
			il.add(end);

			locals.add(new LocalVariableNode("this", parent.thisClassType().getDescriptor(), null, begin, end, 0));
			locals.add(new LocalVariableNode("state", Type.getDescriptor(LuaState.class), null, begin, end, 1));
			locals.add(new LocalVariableNode("sink", Type.getDescriptor(ObjectSink.class), null, begin, end, 2));
			locals.add(new LocalVariableNode("suspendedState", Type.getDescriptor(Object.class), null, begin, end, 3));
			locals.add(new LocalVariableNode("ss", Type.getDescriptor(DefaultSavedState.class), null, vars, end, 4));

			resumeMethodNode.maxStack = 4 + (numOfRegisters() > 0 ? 3: 0);
			resumeMethodNode.maxLocals = 5;
		}
		else
		{
			InsnList il = resumeMethodNode.instructions;
			List<LocalVariableNode> locals = resumeMethodNode.localVariables;

			LabelNode begin = new LabelNode();
			LabelNode end = new LabelNode();

			il.add(begin);
			il.add(new TypeInsnNode(NEW, Type.getInternalName(UnsupportedOperationException.class)));
			il.add(new InsnNode(DUP));
			il.add(ASMUtils.ctor(UnsupportedOperationException.class));
			il.add(new InsnNode(ATHROW));
			il.add(end);

			locals.add(new LocalVariableNode("this", parent.thisClassType().getDescriptor(), null, begin, end, 0));
			locals.add(new LocalVariableNode("state", Type.getDescriptor(LuaState.class), null, begin, end, 1));
			locals.add(new LocalVariableNode("sink", Type.getDescriptor(ObjectSink.class), null, begin, end, 2));
			locals.add(new LocalVariableNode("suspendedState", Type.getDescriptor(Object.class), null, begin, end, 3));

			resumeMethodNode.maxStack = 2;
			resumeMethodNode.maxLocals = 4;
		}
	}

	protected void _error_state() {
		errorState.add(l_error_state);
		errorState.add(new FrameNode(F_SAME, 0, null, 0, null));
		errorState.add(new TypeInsnNode(NEW, Type.getInternalName(IllegalStateException.class)));
		errorState.add(new InsnNode(DUP));
		errorState.add(ASMUtils.ctor(IllegalStateException.class));
		errorState.add(new InsnNode(ATHROW));
	}

	protected boolean isResumable() {
		return resumptionPoints.size() > 1;
	}

	protected void _dispatch_table() {
		if (isResumable()) {
			LabelNode[] labels = resumptionPoints.toArray(new LabelNode[0]);

			resumeSwitch.add(new VarInsnNode(ILOAD, LV_RESUME));
			resumeSwitch.add(new TableSwitchInsnNode(0, resumptionPoints.size() - 1, l_error_state, labels));
		}
	}

	protected int numOfRegisters() {
		return context.prototype().getMaximumStackSize();
	}

	private Type saveStateType() {
		ArrayList<Type> args = new ArrayList<>();

		args.add(Type.INT_TYPE);
		if (isVararg) {
			args.add(ASMUtils.arrayTypeFor(Object.class));
		}
		for (int i = 0; i < numOfRegisters(); i++) {
			args.add(Type.getType(Object.class));
		}
		return Type.getMethodType(Type.getType(Serializable.class), args.toArray(new Type[0]));
	}

	private String saveStateName() {
		return "snapshot";
	}

	private MethodNode saveStateNode() {
		if (isResumable()) {
			MethodNode saveNode = new MethodNode(
					ACC_PRIVATE,
					saveStateName(),
					saveStateType().getDescriptor(),
					null,
					null);

			InsnList il = saveNode.instructions;
			LabelNode begin = new LabelNode();
			LabelNode end = new LabelNode();

			il.add(begin);

			il.add(new TypeInsnNode(NEW, Type.getInternalName(DefaultSavedState.class)));
			il.add(new InsnNode(DUP));

			int regOffset = isVararg ? 3 : 2;

			// resumption point
			il.add(new VarInsnNode(ILOAD, 1));

			// registers
			int numRegs = numOfRegisters();
			il.add(ASMUtils.loadInt(numRegs));
			il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
			for (int i = 0; i < numRegs; i++) {
				il.add(new InsnNode(DUP));
				il.add(ASMUtils.loadInt(i));
				il.add(new VarInsnNode(ALOAD, regOffset + i));
				il.add(new InsnNode(AASTORE));
			}

			// varargs
			if (isVararg) {
				il.add(new VarInsnNode(ALOAD, 2));
			}

			if (isVararg) {
				il.add(ASMUtils.ctor(
						Type.getType(DefaultSavedState.class),
						Type.INT_TYPE,
						ASMUtils.arrayTypeFor(Object.class),
						ASMUtils.arrayTypeFor(Object.class)));
			}
			else {
				il.add(ASMUtils.ctor(
						Type.getType(DefaultSavedState.class),
						Type.INT_TYPE,
						ASMUtils.arrayTypeFor(Object.class)));
			}

			il.add(new InsnNode(ARETURN));

			il.add(end);

			List<LocalVariableNode> locals = saveNode.localVariables;

			locals.add(new LocalVariableNode("this", parent.thisClassType().getDescriptor(), null, begin, end, 0));
			locals.add(new LocalVariableNode("rp", Type.INT_TYPE.getDescriptor(), null, begin, end, 1));
			if (isVararg) {
				locals.add(new LocalVariableNode("varargs", ASMUtils.arrayTypeFor(Object.class).getDescriptor(), null, begin, end, 2));
			}
			for (int i = 0; i < numOfRegisters(); i++) {
				locals.add(new LocalVariableNode("r_" + i, Type.getDescriptor(Object.class), null, begin, end, regOffset + i));
			}

			saveNode.maxLocals = 2 + numOfRegisters();
			saveNode.maxStack = 4 + 3;  // 4 to get register array at top, +3 to add element to it

			return saveNode;
		}
		else {
			return null;
		}
	}

	protected void _resumption_handler() {
		resumeHandler.add(l_handler_begin);
		resumeHandler.add(new FrameNode(F_SAME1, 0, null, 1, new Object[] { Type.getInternalName(ControlThrowable.class) }));

		resumeHandler.add(new InsnNode(DUP));

		resumeHandler.add(new VarInsnNode(ALOAD, 0));  // this

		// create state snapshot
		resumeHandler.add(new VarInsnNode(ALOAD, 0));
		resumeHandler.add(new VarInsnNode(ILOAD, LV_RESUME));
		if (isVararg) {
			resumeHandler.add(new VarInsnNode(ALOAD, LV_VARARGS));
		}
		for (int i = 0; i < numOfRegisters(); i++) {
			resumeHandler.add(new VarInsnNode(ALOAD, registerOffset() + i));
		}
		resumeHandler.add(new MethodInsnNode(
				INVOKESPECIAL,
				parent.thisClassType().getInternalName(),
				saveStateName(),
				saveStateType().getDescriptor(),
				false));

		// register snapshot with the control exception
		resumeHandler.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(ControlThrowable.class),
				"push",
				Type.getMethodType(
						Type.VOID_TYPE,
						Type.getType(Resumable.class),
						Type.getType(Serializable.class)).getDescriptor(),
				false));

		// rethrow
		resumeHandler.add(new InsnNode(ATHROW));

		runMethodNode.tryCatchBlocks.add(new TryCatchBlockNode(l_insns_begin, l_handler_begin, l_handler_begin, Type.getInternalName(ControlThrowable.class)));
	}

	public InsnList getUpvalueReference(int idx) {
		InsnList il = new InsnList();
		il.add(new VarInsnNode(ALOAD, 0));
		il.add(new FieldInsnNode(
				GETFIELD,
				parent.thisClassType().getInternalName(),
				parent.getUpvalueFieldName(idx),
				Type.getDescriptor(Upvalue.class)));
		return il;
	}

	public AbstractInsnNode getUpvalueValue() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(Upvalue.class),
				"get",
				Type.getMethodDescriptor(
						Type.getType(Object.class)),
				false);
	}

	public AbstractInsnNode setUpvalueValue() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(Upvalue.class),
				"set",
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						Type.getType(Object.class)),
				false);
	}

	@Deprecated
	public Type[] closureConstructorTypes(int numUpvalues) {
		Type[] argTypes = new Type[numUpvalues];
		Arrays.fill(argTypes, Type.getType(Upvalue.class));
		return argTypes;
	}

	public InsnList captureRegister(int registerIndex) {
		InsnList il = new InsnList();

		il.add(loadLuaState());
		il.add(loadRegisterValue(registerIndex));
		il.add(LuaStateMethods.newUpvalue());
		il.add(storeRegisterValue(registerIndex));

		return il;
	}

	public InsnList uncaptureRegister(int registerIndex) {
		InsnList il = new InsnList();

		il.add(loadRegisterValue(registerIndex));
		il.add(checkCast(Upvalue.class));
		il.add(getUpvalueValue());
		il.add(storeRegisterValue(registerIndex));

		return il;
	}

	private void _frame_same(InsnList il) {
		il.add(new FrameNode(F_SAME, 0, null, 0, null));
	}

	@Deprecated
	public void _label_here(Object identity) {
		LabelNode l = _l(identity);
		code.add(l);
		_frame_same(code);
	}

	@Deprecated
	public void _goto(Object l) {
		code.add(new JumpInsnNode(GOTO, _l(l)));
	}

	@Deprecated
	public void _next_insn(Object t) {
		_goto(t);
//
//		if (t.inSize() < 2) {
//			// can be inlined, TODO: check this again
//			_note("goto ignored: " + t.toString());
//		}
//		else {
//			_goto(t);
//		}
	}

	public InsnList setReturnValuesFromRegisters(int fromRegisterIndex, SlotState st, int num) {
		InsnList il = new InsnList();

		if (ObjectSinkMethods.canSaveNResults(num)) {
			il.add(loadRegisters(fromRegisterIndex, st, num));
			il.add(ObjectSinkMethods.setTo(num));
		}
		else {
			// need to pack into an array
			il.add(packRegistersIntoArray(fromRegisterIndex, st, num));
			il.add(ObjectSinkMethods.setToArray());
		}

		return il;
	}

	public InsnList setReturnValuesUpToStackTop(int fromRegisterIndex, SlotState st) {
		Check.isTrue(st.hasVarargs());

		InsnList il = new InsnList();

		int varargPosition = st.varargPosition();

		int n = varargPosition - fromRegisterIndex;

		if (n == 0) {
			// nothing to change, it's good as-is!
		}
		else if (n < 0) {
			// drop -n elements from the beginning
			il.add(loadObjectSink());
			il.add(ASMUtils.loadInt(-n));
			il.add(ObjectSinkMethods.drop());
		}
		else {
			// prepend n elements
			il.add(loadObjectSink());

			il.add(ASMUtils.loadInt(n));
			il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
			for (int i = 0; i < n; i++) {
				il.add(new InsnNode(DUP));
				il.add(ASMUtils.loadInt(i));
				il.add(loadRegister(fromRegisterIndex + i, st));
				il.add(new InsnNode(AASTORE));
			}

			il.add(ObjectSinkMethods.prepend());
		}
		return il;
	}

	public void _line_here(int line) {
		LabelNode l = _l(new Object());
		code.add(l);
		code.add(new LineNumberNode(line, l));
	}

	public InsnList loadRegisterAsBoolean(int registerIndex, SlotState slots) {
		InsnList il = new InsnList();
		if (slots.typeAt(registerIndex).isSubtypeOf(LuaTypes.BOOLEAN)) {
			il.add(loadRegister(registerIndex, slots, Boolean.class));
			il.add(booleanValue());
		}
		else {
			il.add(loadRegister(registerIndex, slots));
			il.add(objectToBoolean());
		}
		return il;
	}

	@Deprecated
	public void _bnot(Object id, int r_src, int r_dest, SlotState s) {
		if (s.typeAt(r_src).isSubtypeOf(LuaTypes.NUMBER_INTEGER)) {
			code.add(loadRegister(r_src, s, Number.class));
			code.add(longValue(Number.class));
			code.add(new LdcInsnNode(-1L));
			code.add(new InsnNode(LXOR));
			code.add(ASMUtils.box(Type.LONG_TYPE, Type.getType(Long.class)));
			code.add(storeToRegister(r_dest, s));
		}
		else {
			_save_pc(id);
			code.add(loadDispatchPreamble());
			code.add(loadRegister(r_src, s));
			code.add(dispatchGeneric("bnot", 1));

			_resumptionPoint(id);
			code.add(retrieve_0());
			code.add(storeToRegister(r_dest, s));
		}
	}

	private InsnList nativeBinaryOperationAndBox(int opcode, boolean resultIsLong) {
		InsnList il = new InsnList();

		il.add(new InsnNode(opcode));
		if (resultIsLong) {
			il.add(ASMUtils.box(Type.LONG_TYPE, Type.getType(Long.class)));
		}
		else {
			il.add(ASMUtils.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
		}

		return il;
	}

	private InsnList rawBinaryOperationAndBox(String name, boolean argsAreLong, boolean resultIsLong) {
		InsnList il = new InsnList();

		il.add(new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(RawOperators.class),
				name,
				Type.getMethodDescriptor(
						resultIsLong ? Type.LONG_TYPE : Type.DOUBLE_TYPE,
						argsAreLong ? Type.LONG_TYPE : Type.DOUBLE_TYPE,
						argsAreLong ? Type.LONG_TYPE : Type.DOUBLE_TYPE),
				false));
		if (resultIsLong) {
			il.add(ASMUtils.box(Type.LONG_TYPE, Type.getType(Long.class)));
		}
		else {
			il.add(ASMUtils.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
		}
		return il;
	}

	private InsnList binaryIntegerOperation(LuaBinaryOperation.Op op, SlotState s, int r_dest, int rk_left, int rk_right) {
		InsnList il = new InsnList();
		
		switch (op) {
			case DIV:
			case POW:
				il.add(loadUnboxedRegisterOrConstant(rk_left, s, Type.DOUBLE_TYPE));
				il.add(loadUnboxedRegisterOrConstant(rk_right, s, Type.DOUBLE_TYPE));
				break;

			case SHL:
			case SHR:
				il.add(loadUnboxedRegisterOrConstant(rk_left, s, Type.LONG_TYPE));
				il.add(loadUnboxedRegisterOrConstant(rk_right, s, Type.INT_TYPE));
				break;

			default:
				il.add(loadUnboxedRegisterOrConstant(rk_left, s, Type.LONG_TYPE));
				il.add(loadUnboxedRegisterOrConstant(rk_right, s, Type.LONG_TYPE));
				break;
		}

		switch (op) {
			case ADD:  il.add(nativeBinaryOperationAndBox(LADD, true)); break;
			case SUB:  il.add(nativeBinaryOperationAndBox(LSUB, true)); break;
			case MUL:  il.add(nativeBinaryOperationAndBox(LMUL, true)); break;
			case MOD:  il.add(rawBinaryOperationAndBox("rawmod", true, true)); break;
			case POW:  il.add(rawBinaryOperationAndBox("rawpow", false, false)); break;
			case DIV:  il.add(nativeBinaryOperationAndBox(DDIV, false)); break;
			case IDIV: il.add(rawBinaryOperationAndBox("rawidiv", true, true)); break;
			case BAND: il.add(nativeBinaryOperationAndBox(LAND, true)); break;
			case BOR:  il.add(nativeBinaryOperationAndBox(LOR, true)); break;
			case BXOR: il.add(nativeBinaryOperationAndBox(LXOR, true)); break;
			case SHL:  il.add(nativeBinaryOperationAndBox(LSHL, true)); break;
			case SHR:  il.add(nativeBinaryOperationAndBox(LUSHR, true)); break;
			default: throw new IllegalStateException("Illegal op: " + op);
		}

		il.add(storeToRegister(r_dest, s));

		return il;
	}

	private InsnList binaryFloatOperation(LuaBinaryOperation.Op op, SlotState s, int r_dest, int rk_left, int rk_right) {
		InsnList il = new InsnList();

		il.add(loadUnboxedRegisterOrConstant(rk_left, s, Type.DOUBLE_TYPE));
		il.add(loadUnboxedRegisterOrConstant(rk_right, s, Type.DOUBLE_TYPE));

		switch (op) {
			case ADD:  il.add(nativeBinaryOperationAndBox(DADD, false)); break;
			case SUB:  il.add(nativeBinaryOperationAndBox(DSUB, false)); break;
			case MUL:  il.add(nativeBinaryOperationAndBox(DMUL, false)); break;
			case MOD:  il.add(rawBinaryOperationAndBox("rawmod", false, false)); break;
			case POW:  il.add(rawBinaryOperationAndBox("rawpow", false, false)); break;
			case DIV:  il.add(nativeBinaryOperationAndBox(DDIV, false)); break;
			case IDIV: il.add(rawBinaryOperationAndBox("rawidiv", false, false)); break;
			default: throw new IllegalStateException("Illegal op: " + op);
		}

		il.add(storeToRegister(r_dest, s));

		return il;
	}

	private InsnList binaryNumericOperation(LuaBinaryOperation.Op op, SlotState s, int r_dest, int rk_left, int rk_right) {
		InsnList il = new InsnList();

		String method = op.name().toLowerCase();  // FIXME: brittle
		il.add(loadRegisterOrConstant(rk_left, s, Number.class));
		il.add(loadRegisterOrConstant(rk_right, s, Number.class));
		il.add(dispatchNumeric(method, 2));
		il.add(storeToRegister(r_dest, s));

		return il;
	}

	@Deprecated
	private void _binary_dynamic_op(LuaBinaryOperation.Op op, SlotState s, int r_dest, int rk_left, int rk_right) {
		Object id = new Object();
		String method = op.name().toLowerCase();  // FIXME: brittle

		_save_pc(id);

		code.add(loadDispatchPreamble());
		code.add(loadRegisterOrConstant(rk_left, s));
		code.add(loadRegisterOrConstant(rk_right, s));
		code.add(dispatchGeneric(method, 2));

		_resumptionPoint(id);

		code.add(retrieve_0());
		code.add(storeToRegister(r_dest, s));
	}

	@Deprecated
	public void binaryOperation(LuaBinaryOperation.Op op, SlotState s, int r_dest, int rk_left, int rk_right) {
		InsnList il = code;  // FIXME

		StaticMathImplementation staticMath = LuaBinaryOperation.mathForOp(op);
		LuaInstruction.NumOpType ot = staticMath.opType(
				LuaBinaryOperation.slotType(context(), s, rk_left),
				LuaBinaryOperation.slotType(context(), s, rk_right));

		switch (ot) {
			case Integer: il.add(binaryIntegerOperation(op, s, r_dest, rk_left, rk_right)); break;
			case Float:   il.add(binaryFloatOperation(op, s, r_dest, rk_left, rk_right)); break;
			case Number:  il.add(binaryNumericOperation(op, s, r_dest, rk_left, rk_right)); break;
			case Any:     _binary_dynamic_op(op, s, r_dest, rk_left, rk_right); break;
		}
	}

	public AbstractInsnNode loadVarargs() {
		Check.isTrue(isVararg);
		return new VarInsnNode(ALOAD, LV_VARARGS);
	}

	@Deprecated
	public void _cmp(Object id, String methodName, int rk_left, int rk_right, boolean pos, SlotState s, Object trueBranch, Object falseBranch) {

		// TODO: specialise

		_save_pc(id);

		code.add(loadDispatchPreamble());
		code.add(loadRegisterOrConstant(rk_left, s));
		code.add(loadRegisterOrConstant(rk_right, s));
		code.add(dispatchGeneric(methodName, 2));

		_resumptionPoint(id);
		code.add(retrieve_0());

		// assuming that _0 is of type Boolean.class

		code.add(checkCast(Boolean.class));
		code.add(booleanValue());

		// compare stack top with the expected value -- branch if not equal
		code.add(new JumpInsnNode(pos ? IFEQ : IFNE, _l(falseBranch)));

		// TODO: this could be a fall-through rather than a jump!
		code.add(new JumpInsnNode(GOTO, _l(trueBranch)));
	}

	public static AbstractInsnNode objectToBoolean() {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Conversions.class),
				"objectToBoolean",
				Type.getMethodDescriptor(
						Type.BOOLEAN_TYPE,
						Type.getType(Object.class)),
				false);
	}

	@Deprecated
	public void _ifzero(Object tgt) {
		code.add(new JumpInsnNode(IFEQ, _l(tgt)));
	}

	@Deprecated
	public void _ifnonzero(Object tgt) {
		code.add(new JumpInsnNode(IFNE, _l(tgt)));
	}

	private InsnList convertNumericRegisterToFloat(int registerIndex, SlotState st) {
		InsnList il = new InsnList();

		il.add(loadRegister(registerIndex, st, Number.class));
		il.add(doubleValue(Number.class));
		il.add(ASMUtils.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
		il.add(storeToRegister(registerIndex, st));

		return il;
	}

	// TODO: name: shouldn't this be "coerce"?
	private InsnList convertRegisterToNumber(int r, SlotState st, String what) {
		InsnList il = new InsnList();

		il.add(loadRegister(r, st));
		il.add(objectToNumber(what));
		il.add(storeToRegister(r, st));

		return il;
	}

	public static AbstractInsnNode booleanValue() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(Boolean.class),
				"booleanValue",
				Type.getMethodDescriptor(
						Type.BOOLEAN_TYPE),
				false);
	}

	private static AbstractInsnNode intValue(Class clazz) {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(clazz),
				"intValue",
				Type.getMethodDescriptor(
						Type.INT_TYPE),
				false);
	}

	private static AbstractInsnNode longValue(Class clazz) {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(clazz),
				"longValue",
				Type.getMethodDescriptor(
						Type.LONG_TYPE),
				false);
	}

	private static AbstractInsnNode doubleValue(Class clazz) {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(clazz),
				"doubleValue",
				Type.getMethodDescriptor(
						Type.DOUBLE_TYPE),
				false);
	}

	private InsnList objectToNumber(String what) {
		Check.notNull(what);
		InsnList il = new InsnList();

		il.add(new LdcInsnNode(what));
		il.add(new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Conversions.class),
				"objectToNumber",
				Type.getMethodDescriptor(
						Type.getType(Number.class),
						Type.getType(Object.class),
						Type.getType(String.class)),
				false));

		return il;
	}

	@Deprecated
	public void _forprep(SlotState st, int r_base) {
		final InsnList il = code;  // FIXME

		LuaInstruction.NumOpType loopType = LuaInstruction.NumOpType.loopType(
				st.typeAt(r_base + 0),
				st.typeAt(r_base + 1),
				st.typeAt(r_base + 2));

		switch (loopType) {
			case Integer:
				// the initial decrement

				// convert to number if necessary
				if (!st.typeAt(r_base + 1).isSubtypeOf(LuaTypes.NUMBER)) {
					il.add(convertRegisterToNumber(r_base + 1, st, "'for' limit"));
				}

				il.add(loadRegister(r_base, st, Number.class));
				il.add(longValue(Number.class));
				il.add(loadRegister(r_base + 2, st, Number.class));
				il.add(longValue(Number.class));
				il.add(new InsnNode(LSUB));
				il.add(ASMUtils.box(Type.LONG_TYPE, Type.getType(Long.class)));
				il.add(storeToRegister(r_base, st));
				break;

			case Float:
				// convert to number if necessary
				if (!st.typeAt(r_base + 1).isSubtypeOf(LuaTypes.NUMBER)) {
					il.add(convertRegisterToNumber(r_base + 1, st, "'for' limit"));
				}

				// convert to float when necessary (we are in a float loop, so both of these parameters
				// are numbers, and at least one of them is a float)

				if (!st.typeAt(r_base + 0).isSubtypeOf(LuaTypes.NUMBER_FLOAT)) {
					il.add(convertNumericRegisterToFloat(r_base + 0, st));
				}
				if (!st.typeAt(r_base + 2).isSubtypeOf(LuaTypes.NUMBER_FLOAT)) {
					il.add(convertNumericRegisterToFloat(r_base + 2, st));
				}

				// the initial decrement
				il.add(loadRegister(r_base, st, Number.class));
				il.add(doubleValue(Number.class));
				il.add(loadRegister(r_base + 2, st, Number.class));
				il.add(doubleValue(Number.class));
				il.add(new InsnNode(DSUB));
				il.add(ASMUtils.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
				il.add(storeToRegister(r_base, st));
				break;

			case Number:
				// We were unable to statically determine loop kind: force conversion of loop
				// parameters. Note that this does *not* imply that this is a float loop.

				// Note: we process parameters in the same order as in PUC Lua to get
				// the same error reporting.

				il.add(convertRegisterToNumber(r_base + 1, st, "'for' limit"));

				il.add(loadRegister(r_base + 2, st));
				il.add(objectToNumber("'for' step"));
				il.add(new InsnNode(DUP));
				il.add(storeToRegister(r_base + 2, st));
				il.add(loadRegister(r_base, st));
				il.add(objectToNumber("'for' initial value"));
				il.add(new InsnNode(SWAP));
				il.add(dispatchNumeric("sub", 2));
				il.add(storeToRegister(r_base, st));
				break;

			default:
				throw new IllegalStateException("Illegal loop type: " + loopType + " (base: " + r_base + "; slot state: " + st + ")");
		}
	}

	private FrameNode _fullFrame(int numStack, Object[] stack) {
		ArrayList<Object> locals = new ArrayList<>();
		locals.add(parent.thisClassType().getInternalName());
		locals.add(Type.getInternalName(LuaState.class));
		locals.add(Type.getInternalName(ObjectSink.class));
		locals.add(INTEGER);
		if (isVararg) {
			locals.add(ASMUtils.arrayTypeFor(Object.class).getInternalName());
		}
		for (int i = 0; i < numOfRegisters(); i++) {
			locals.add(Type.getInternalName(Object.class));
		}

		return new FrameNode(
				F_FULL,
				locals.size(),
				locals.toArray(),
				numStack, stack);
	}

	@Deprecated
	public void _forloop(SlotState st, int r_base, Object continueBranch, Object breakBranch) {
		final InsnList il = code;  // FIXME

		net.sandius.rembulan.compiler.types.Type a0 = st.typeAt(r_base + 0);  // index
		net.sandius.rembulan.compiler.types.Type a1 = st.typeAt(r_base + 1);  // limit
		net.sandius.rembulan.compiler.types.Type a2 = st.typeAt(r_base + 2);  // step

		LabelNode ascendingLoop = new LabelNode();
		LabelNode descendingLoop = new LabelNode();

		LuaInstruction.NumOpType loopType = LuaInstruction.NumOpType.loopType(a0, a1, a2);

		switch (loopType) {

			case Integer:

				// increment
				il.add(loadRegister(r_base, st, Number.class));
				il.add(longValue(Number.class));
				il.add(loadRegister(r_base + 2, st, Number.class));
				il.add(longValue(Number.class));
				il.add(new InsnNode(LADD));

				if (a1.isSubtypeOf(LuaTypes.NUMBER_INTEGER)) {
					il.add(new InsnNode(DUP2));  // will re-use this value for comparison
				}

				// box and store
				il.add(ASMUtils.box(Type.LONG_TYPE, Type.getType(Long.class)));
				il.add(storeToRegister(r_base, st));  // save into register

				if (a1.isSubtypeOf(LuaTypes.NUMBER_INTEGER)) {
					il.add(loadRegister(r_base + 1, st, Number.class));
					il.add(longValue(Number.class));
					il.add(new InsnNode(LCMP));

					// Stack here: I(lcmp(index, limit))

					// We now have the integer representing the comparison of index and limit
					// on the stack. To interpret this value, we now need to determine whether
					// we're in an ascending or descending loop.

					// compare step with zero
					il.add(loadRegister(r_base + 2, st, Number.class));
					il.add(longValue(Number.class));
					il.add(new InsnNode(LCONST_0));
					il.add(new InsnNode(LCMP));

					// Stack here: I(lcmp(index, limit)) I(lcmp(step, 0))

					il.add(new InsnNode(DUP));
					il.add(new JumpInsnNode(IFGT, ascendingLoop));
					il.add(new JumpInsnNode(IFLT, descendingLoop));
					il.add(new InsnNode(POP));  // we won't be needing the comparison value
					il.add(new JumpInsnNode(GOTO, _l(breakBranch)));  // zero-step: break

					il.add(descendingLoop);
					// Stack here: I(lcmp(index, limit))
					il.add(new FrameNode(F_SAME1, 0, null, 1, new Object[] { INTEGER }));
					il.add(new JumpInsnNode(IFLT, _l(breakBranch)));  // descending: break if lesser than limit
					il.add(new JumpInsnNode(GOTO, _l(continueBranch)));

					il.add(ascendingLoop);
					// Stack here: I(lcmp(index, limit)) I(lcmp(step, 0))
					// FIXME: do we really need to dump a full frame?
					il.add(_fullFrame(2, new Object[] { INTEGER, INTEGER }));
					il.add(new InsnNode(POP));
					il.add(new JumpInsnNode(IFGT, _l(breakBranch)));  // ascending: break if greater than limit
					il.add(new JumpInsnNode(GOTO, _l(continueBranch)));
				}
				else {
					// limit is not statically known to be an integer

					// Stack here: empty

					il.add(loadRegister(r_base + 0, st, Number.class));
					il.add(loadRegister(r_base + 1, st, Number.class));
					il.add(loadRegister(r_base + 2, st, Number.class));
					il.add(new MethodInsnNode(
							INVOKESTATIC,
							Type.getInternalName(Dispatch.class),
							"continueLoop",
							Type.getMethodDescriptor(
									Type.BOOLEAN_TYPE,
									Type.getType(Number.class),
									Type.getType(Number.class),
									Type.getType(Number.class)),
							false));

					il.add(new JumpInsnNode(IFEQ, _l(breakBranch)));
					il.add(new JumpInsnNode(GOTO, _l(continueBranch)));
				}

				break;

			case Float:

				// increment index
				il.add(loadRegister(r_base, st, Number.class));
				il.add(doubleValue(Number.class));
				il.add(loadRegister(r_base + 2, st, Number.class));
				il.add(doubleValue(Number.class));
				il.add(new InsnNode(DADD));
				il.add(new InsnNode(DUP2));  // will re-use this value for comparison

				il.add(ASMUtils.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
				il.add(storeToRegister(r_base, st));  // save index into register

				// push limit to the stack
				il.add(loadRegister(r_base + 1, st, Number.class));
				il.add(doubleValue(Number.class));

				// Stack here: D(index) D(limit)

				// At this point we have the index and the limit on the stack.
				// Next, we need to determine what kind of loop we're in. Only then can we make
				// the comparison -- at this point we wouldn't know how to treat a possible NaN result!

				LabelNode stepIsNan = new LabelNode();

				// fetch the step
				il.add(loadRegister(r_base + 2, st, Number.class));
				il.add(doubleValue(Number.class));
				il.add(new InsnNode(DUP2));  // save it for later use

				// test step for NaN
				il.add(new InsnNode(DUP2));
				il.add(new InsnNode(DCMPG));  // compare with self: result will be non-zero iff step is not NaN
				il.add(new JumpInsnNode(IFNE, stepIsNan));

				// Stack here: D(index) D(limit) D(step)

				// compare step with 0.0
				il.add(new InsnNode(DCONST_0));
				il.add(new InsnNode(DCMPG));

				// Stack here: D(index) D(limit) I(dcmpg(step,0.0))

				il.add(new InsnNode(DUP));
				il.add(new JumpInsnNode(IFGT, ascendingLoop));
				il.add(new JumpInsnNode(IFLT, descendingLoop));

				// Stack here: D(index) D(limit)

				il.add(new InsnNode(POP2));
				il.add(new InsnNode(POP2));
				il.add(new JumpInsnNode(GOTO, _l(breakBranch)));  // step is zero => break

				// step is NaN => break
				il.add(stepIsNan);
				// Stack here: D(index) D(limit) D(step)
				il.add(_fullFrame(3, new Object[] { DOUBLE, DOUBLE, DOUBLE }));
				il.add(new InsnNode(POP2));
				il.add(new InsnNode(POP2));
				il.add(new InsnNode(POP2));
				il.add(new JumpInsnNode(GOTO, _l(breakBranch)));

				il.add(descendingLoop);
				// Stack here: D(index) D(limit)
				il.add(_fullFrame(2, new Object[] { DOUBLE, DOUBLE }));
				il.add(new InsnNode(DCMPL));  // if index or limit is NaN, result in -1
				il.add(new JumpInsnNode(IFLT, _l(breakBranch)));  // descending: break if lesser than limit
				il.add(new JumpInsnNode(GOTO, _l(continueBranch)));

				il.add(ascendingLoop);
				// Stack here: D(index) D(limit) I(dcmpg(step,0.0))
				il.add(_fullFrame(3, new Object[] { DOUBLE, DOUBLE, INTEGER }));
				il.add(new InsnNode(POP));
				il.add(new InsnNode(DCMPG));  // if index or limit is NaN, result in +1
				il.add(new JumpInsnNode(IFGT, _l(breakBranch)));  // ascending: break if greater than limit
				il.add(new JumpInsnNode(GOTO, _l(continueBranch)));
				break;

			case Number:

				// increment index
				il.add(loadRegister(r_base, st, Number.class));
				il.add(loadRegister(r_base + 2, st, Number.class));
				il.add(dispatchNumeric("add", 2));
				il.add(new InsnNode(DUP));
				il.add(storeToRegister(r_base, st));  // save index into register

				il.add(loadRegister(r_base + 1, st, Number.class));
				il.add(loadRegister(r_base + 2, st, Number.class));
				il.add(dispatchContinueLoop());

				il.add(new JumpInsnNode(IFEQ, _l(breakBranch)));
				il.add(new JumpInsnNode(GOTO, _l(continueBranch)));
				break;

			default:
				throw new IllegalStateException("Illegal loop type: " + loopType + " (base: " + r_base + "; slot state: " + st + ")");
		}
	}

	public CodeVisitor codeVisitor() {
		return new JavaBytecodeCodeVisitor(this);
	}

}

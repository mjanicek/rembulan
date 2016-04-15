package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.compiler.gen.CodeVisitor;
import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.compiler.gen.PrototypeContext;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
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
import org.objectweb.asm.tree.LabelNode;
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

	public InsnList loadConstant(int idx, Class castTo) {
		return BoxedPrimitivesMethods.loadBoxedConstant(context.getConst(idx), castTo);
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
		il.add(ASMUtils.checkCast(castTo));
		return il;
	}

	public InsnList loadRegister(int registerIndex, SlotState slots, Class<?> castTo) {
		Check.notNull(slots);
		Check.nonNegative(registerIndex);

		InsnList il = new InsnList();

		if (slots.isCaptured(registerIndex)) {
			il.add(loadRegisterValue(registerIndex, Upvalue.class));
			il.add(UpvalueMethods.get());
		}
		else {
			il.add(loadRegisterValue(registerIndex));
		}

		Class clazz = Object.class;

		if (castTo != null && !castTo.isAssignableFrom(clazz)) {
			il.add(ASMUtils.checkCast(castTo));
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

	public InsnList loadNumericRegisterOrConstantValue(int rk, SlotState slots, Type requiredType) {
		InsnList il = new InsnList();

		// FIXME: this duplicates the retrieval code!
		if (rk < 0) {
			// it's a constant
			int constIndex = -rk - 1;
			Object c = context.getConst(constIndex);
			if (c instanceof Number) {
				il.add(BoxedPrimitivesMethods.loadNumericValue((Number) c, requiredType));
			}
			else {
				throw new IllegalArgumentException("Constant #" + constIndex + " is not a Number: "
						+ c + " (" + (c != null ? c.getClass().getName() : "null") + ")");
			}
		}
		else {
			// it's a register
			il.add(loadRegister(rk, slots, Number.class));
			il.add(BoxedPrimitivesMethods.unbox(Number.class, requiredType));
		}

		return il;
	}

	private AbstractInsnNode storeRegisterValue(int registerIndex) {
		return new VarInsnNode(ASTORE, registerOffset() + registerIndex);
	}

	public InsnList storeToRegister(int registerIndex, SlotState slots) {
		InsnList il = new InsnList();

		if (slots.isCaptured(registerIndex)) {
			il.add(loadRegisterValue(registerIndex, Upvalue.class));
			il.add(new InsnNode(SWAP));
			il.add(UpvalueMethods.set());
		}
		else {
			il.add(storeRegisterValue(registerIndex));
		}

		return il;
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

	public class ResumptionPoint {

		public final int index;

		private ResumptionPoint(int index) {
			this.index = index;
		}

		public LabelNode label() {
			return _l(this);
		}

		public InsnList save() {
			InsnList il = new InsnList();
			il.add(ASMUtils.loadInt(index));
			il.add(new VarInsnNode(ISTORE, LV_RESUME));
			return il;
		}

		public InsnList resume() {
			InsnList il = new InsnList();

			il.add(label());
			il.add(ASMUtils.frameSame());

			return il;
		}
	}

	public ResumptionPoint resumptionPoint() {
		int idx = resumptionPoints.size();
		ResumptionPoint rp = new ResumptionPoint(idx);
		resumptionPoints.add(rp.label());
		return rp;
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
		code.add(ASMUtils.frameSame());

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
		errorState.add(ASMUtils.frameSame());
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
		resumeHandler.add(ASMUtils.frameSame1(ControlThrowable.class));

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

		il.add(loadRegisterValue(registerIndex, Upvalue.class));
		il.add(UpvalueMethods.get());
		il.add(storeRegisterValue(registerIndex));

		return il;
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
			il.add(BoxedPrimitivesMethods.booleanValue());
		}
		else {
			il.add(loadRegister(registerIndex, slots));
			il.add(UtilMethods.objectToBoolean());
		}
		return il;
	}

	public AbstractInsnNode loadVarargs() {
		Check.isTrue(isVararg);
		return new VarInsnNode(ALOAD, LV_VARARGS);
	}

	public InsnList convertNumericRegisterToFloat(int registerIndex, SlotState st) {
		InsnList il = new InsnList();

		il.add(loadRegister(registerIndex, st, Number.class));
		il.add(BoxedPrimitivesMethods.doubleValue(Number.class));
		il.add(BoxedPrimitivesMethods.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
		il.add(storeToRegister(registerIndex, st));

		return il;
	}

	// TODO: name: shouldn't this be "coerce"?
	public InsnList convertRegisterToNumber(int r, SlotState st, String what) {
		InsnList il = new InsnList();

		il.add(loadRegister(r, st));
		il.add(UtilMethods.objectToNumber(what));
		il.add(storeToRegister(r, st));

		return il;
	}

	public FrameNode fullFrame(int numStack, Object[] stack) {
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

	public CodeVisitor codeVisitor() {
		return new JavaBytecodeCodeVisitor(this);
	}

}

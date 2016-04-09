package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.PrototypeContext;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.Resumable;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.core.impl.DefaultSavedState;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.asm.ASMUtils;
import org.objectweb.asm.Type;
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

	public final int REGISTER_OFFSET = 4;

	public final int LV_STATE = 1;
	public final int LV_OBJECTSINK = 2;
	public final int LV_RESUME = 3;

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
		Type[] args = new Type[3 + numOfRegisters()];
		args[0] = Type.getType(LuaState.class);
		args[1] = Type.getType(ObjectSink.class);
		args[2] = Type.INT_TYPE;
		for (int i = 3; i < args.length; i++) {
			args[i] = Type.getType(Object.class);
		}
		return Type.getMethodType(Type.VOID_TYPE, args);
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

	public void _dup() {
		code.add(new InsnNode(DUP));
	}

	public void _swap() {
		code.add(new InsnNode(SWAP));
	}

	public void _push_this() {
		code.add(new VarInsnNode(ALOAD, 0));
	}

	public void _push_null() {
		code.add(new InsnNode(ACONST_NULL));
	}

	public void _load_constant(Object k, Class castTo) {
		if (k == null) {
			_push_null();
		}
		else if (k instanceof Boolean) {
			code.add(ASMUtils.loadInt((Boolean) k ? 1 : 0));
			code.add(ASMUtils.box(Type.BOOLEAN_TYPE, Type.getType(Boolean.class)));
		}
		else if (k instanceof Double || k instanceof Float) {
			code.add(ASMUtils.loadDouble(((Number) k).doubleValue()));
			code.add(ASMUtils.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
		}
		else if (k instanceof Number) {
			code.add(ASMUtils.loadLong(((Number) k).longValue()));
			code.add(ASMUtils.box(Type.LONG_TYPE, Type.getType(Long.class)));
		}
		else if (k instanceof String) {
			code.add(new LdcInsnNode((String) k));
		}
		else {
			throw new UnsupportedOperationException("Illegal const type: " + k.getClass());
		}

		if (castTo != null) {
			if (!castTo.isAssignableFrom(k.getClass())) {
				_checkCast(castTo);
			}
		}
	}

	public void _load_constant(Object k) {
		_load_constant(k, null);
	}

	public void _load_k(int idx, Class castTo) {
		_load_constant(context.getConst(idx), castTo);
	}

	public void _load_k(int idx) {
		_load_k(idx, null);
	}

	public void _load_reg_value(int idx) {
		code.add(new VarInsnNode(ALOAD, REGISTER_OFFSET + idx));
	}

	public void _load_reg_value(int idx, Class clazz) {
		_load_reg_value(idx);
		_checkCast(clazz);
	}

	public void _load_reg(int idx, SlotState slots, Class castTo) {
		Check.notNull(slots);
		Check.nonNegative(idx);

		if (slots.isCaptured(idx)) {
			_get_downvalue(idx);
			_get_upvalue_value();
		}
		else {
			_load_reg_value(idx);
		}

		Class clazz = Object.class;

		if (castTo != null) {
			if (!castTo.isAssignableFrom(clazz)) {
				_checkCast(castTo);
			}
		}
	}

	public void _load_reg(int idx, SlotState slots) {
		_load_reg(idx, slots, null);
	}

	public void _load_regs(int firstIdx, SlotState slots, int num) {
		for (int i = 0; i < num; i++) {
			_load_reg(firstIdx + i, slots);
		}
	}

	public void _pack_regs(int firstIdx, SlotState slots, int num) {
		code.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
		for (int i = 0; i < num; i++) {
			code.add(new InsnNode(DUP));
			code.add(ASMUtils.loadInt(i));
			_load_reg(firstIdx + i, slots);
		}
	}

	public void _get_downvalue(int idx) {
		code.add(new VarInsnNode(ALOAD, REGISTER_OFFSET + idx));
		_checkCast(Upvalue.class);
	}

	public void _load_reg_or_const(int rk, SlotState slots, Class castTo) {
		Check.notNull(slots);

		if (rk < 0) {
			// it's a constant
			_load_k(-rk - 1, castTo);
		}
		else {
			_load_reg(rk, slots, castTo);
		}
	}

	public void _load_reg_or_const(int rk, SlotState slots) {
		_load_reg_or_const(rk, slots, null);
	}

	private void _store_reg_value(int r) {
		code.add(new VarInsnNode(ASTORE, REGISTER_OFFSET + r));
	}

	public void _store(int r, SlotState slots) {
		Check.notNull(slots);

		if (slots.isCaptured(r)) {
			_get_downvalue(r);
			_swap();
			_set_upvalue_value();
		}
		else {
			_store_reg_value(r);
		}
	}

	public void _invokeStatic(Class clazz, String methodName, Type methodSignature) {
		code.add(new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(clazz),
				methodName,
				methodSignature.getDescriptor(),
				false
		));
	}

	public void _invokeVirtual(Class clazz, String methodName, Type methodSignature) {
		code.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(clazz),
				methodName,
				methodSignature.getDescriptor(),
				false
		));
	}

	public void _invokeInterface(Class clazz, String methodName, Type methodSignature) {
		code.add(new MethodInsnNode(
				INVOKEINTERFACE,
				Type.getInternalName(clazz),
				methodName,
				methodSignature.getDescriptor(),
				true
		));
	}

	public void _invokeSpecial(String className, String methodName, Type methodSignature) {
		code.add(new MethodInsnNode(
				INVOKESPECIAL,
				ASMUtils.typeForClassName(className).getInternalName(),
				methodName,
				methodSignature.getDescriptor(),
				false
		));
	}

	public void _dispatch_binop(String name, Class clazz) {
		Type t = Type.getType(clazz);
		_invokeStatic(Dispatch.class, name, Type.getMethodType(t, t, t));
	}

	public void _dispatch_generic_mt_2(String name) {
		_invokeStatic(
				Dispatch.class,
				name,
				Type.getMethodType(
					Type.VOID_TYPE,
					Type.getType(LuaState.class),
					Type.getType(ObjectSink.class),
					Type.getType(Object.class),
					Type.getType(Object.class)
			)
		);
	}

	public void _dispatch_index() {
		_dispatch_generic_mt_2("index");
	}

	public void _dispatch_call(int kind) {
		code.add(new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				"call",
				ClassEmitter.methodTypeForKind(kind).getDescriptor(),
				false
		));
	}

	public void _checkCast(Class clazz) {
		code.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(clazz)));
	}

	public void _loadState() {
		withLuaState(code)
				.push();
	}

	public void _loadObjectSink() {
		withObjectSink(code)
				.push();
	}

	public void _retrieve_0() {
		withObjectSink(code)
				.push()
				.call_get(0);
	}

	public void _retrieve_and_store_n(int n, int firstIdx, SlotState s) {
		ObjectSink_prx os = withObjectSink(code);

		for (int i = 0; i < n; i++) {
			os.push().call_get(i);
			_store(firstIdx + i, s);
		}
	}

	public void _save_pc(Object o) {
		LabelNode rl = _l(o);

		int idx = resumptionPoints.size();
		resumptionPoints.add(rl);

		code.add(ASMUtils.loadInt(idx));
		code.add(new VarInsnNode(ISTORE, LV_RESUME));
	}

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

		for (int i = 0; i < numOfRegisters(); i++) {
			locals.add(new LocalVariableNode("r_" + i, Type.getDescriptor(Object.class), null, l_insns_begin, l_insns_end, REGISTER_OFFSET + i));
		}

//		if (isResumable()) {
//			locals.add(new LocalVariableNode("ct", Type.getDescriptor(ControlThrowable.class), null, l_handler_begin, l_handler_end, REGISTER_OFFSET + numOfRegisters()));
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

		int invokeKind = ClassEmitter.kind(numOfParameters, isVararg);


		il.add(begin);

		il.add(new VarInsnNode(ALOAD, 0));  // this
		il.add(new VarInsnNode(ALOAD, 1));  // state
		il.add(new VarInsnNode(ALOAD, 2));  // sink
		il.add(ASMUtils.loadInt(0));  // resumption point

		if (invokeKind < 0) {
			throw new UnsupportedOperationException(); // TODO
		}
		else {
			// we have #invokeKind standalone parameters, mapping them onto #numOfRegisters

			for (int i = 0; i < numOfRegisters(); i++) {
				if (i < invokeKind) {
					il.add(new VarInsnNode(ALOAD, 3 + i));
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
		Type[] args = new Type[1 + numOfRegisters()];
		args[0] = Type.INT_TYPE;
		for (int i = 1; i < args.length; i++) {
			args[i] = Type.getType(Object.class);
		}
		return Type.getMethodType(Type.getType(Serializable.class), args);
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

			// resumption point
			il.add(new VarInsnNode(ILOAD, 1));

			// registers
			int numRegs = numOfRegisters();
			il.add(ASMUtils.loadInt(numRegs));
			il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
			for (int i = 0; i < numRegs; i++) {
				il.add(new InsnNode(DUP));
				il.add(ASMUtils.loadInt(i));
				il.add(new VarInsnNode(ALOAD, 2 + i));
				il.add(new InsnNode(AASTORE));
			}

			// TODO: varargs

			il.add(ASMUtils.ctor(
					Type.getType(DefaultSavedState.class),
					Type.INT_TYPE,
					ASMUtils.arrayTypeFor(Object.class)));

			il.add(new InsnNode(ARETURN));

			il.add(end);

			List<LocalVariableNode> locals = saveNode.localVariables;

			locals.add(new LocalVariableNode("this", parent.thisClassType().getDescriptor(), null, begin, end, 0));
			locals.add(new LocalVariableNode("rp", Type.INT_TYPE.getDescriptor(), null, begin, end, 1));
			for (int i = 0; i < numOfRegisters(); i++) {
				locals.add(new LocalVariableNode("r_" + i, Type.getDescriptor(Object.class), null, begin, end, 2 + i));
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
		for (int i = 0; i < numOfRegisters(); i++) {
			resumeHandler.add(new VarInsnNode(ALOAD, REGISTER_OFFSET + i));
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

	public void _get_upvalue_ref(int idx) {
		code.add(new VarInsnNode(ALOAD, 0));
		code.add(new FieldInsnNode(
				GETFIELD,
				parent.thisClassType().getInternalName(),
				parent.getUpvalueFieldName(idx),
				Type.getDescriptor(Upvalue.class)));
	}

	public void _get_upvalue_value() {
		_invokeVirtual(Upvalue.class, "get", Type.getMethodType(Type.getType(Object.class)));
	}

	public void _set_upvalue_value() {
		_invokeVirtual(Upvalue.class, "set", Type.getMethodType(Type.VOID_TYPE, Type.getType(Object.class)));
	}

	public void _return() {
		code.add(new InsnNode(RETURN));
	}

	public void _new(String className) {
		code.add(new TypeInsnNode(NEW, ASMUtils.typeForClassName(className).getInternalName()));
	}

	public void _new(Class clazz) {
		_new(clazz.getName());
	}

	public void _closure_ctor(String className, int numUpvalues) {
		Type[] argTypes = new Type[numUpvalues];
		Arrays.fill(argTypes, Type.getType(Upvalue.class));
		code.add(ASMUtils.ctor(ASMUtils.typeForClassName(className), argTypes));
	}

	public void _capture(int idx) {
		LuaState_prx state = withLuaState(code);
		state.push();
		_load_reg_value(idx);
		state.call_newUpvalue();
		_store_reg_value(idx);
	}

	public void _uncapture(int idx) {
		_load_reg_value(idx);
		_get_upvalue_value();
		_store_reg_value(idx);
	}

	private void _frame_same(InsnList il) {
		il.add(new FrameNode(F_SAME, 0, null, 0, null));
	}

	public void _label_here(Object identity) {
		LabelNode l = _l(identity);
		code.add(l);
		_frame_same(code);
	}

	public void _goto(Object l) {
		code.add(new JumpInsnNode(GOTO, _l(l)));
	}

	public void _next_insn(Target t) {
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

	public void _new_table(int array, int hash) {
		withLuaState(code)
				.push()
				.do_newTable(array, hash);
	}

	public void _if_null(Object target) {
		code.add(new JumpInsnNode(IFNULL, _l(target)));
	}

	public void _if_nonnull(Object target) {
		code.add(new JumpInsnNode(IFNONNULL, _l(target)));
	}

	public void _tailcall(int numArgs) {
		withObjectSink(code)
				.call_tailCall(numArgs);
	}

	public void _setret(int num) {
		withObjectSink(code)
				.call_setTo(num);
	}

	public void _line_here(int line) {
		LabelNode l = _l(new Object());
		code.add(l);
		code.add(new LineNumberNode(line, l));
	}

	private LuaState_prx withLuaState(InsnList il) {
		return new LuaState_prx(LV_STATE, il);
	}

	private ObjectSink_prx withObjectSink(InsnList il) {
		return new ObjectSink_prx(LV_OBJECTSINK, il);
	}

	private static class LuaState_prx {

		private final int selfIndex;
		private final InsnList il;

		public LuaState_prx(int selfIndex, InsnList il) {
			this.selfIndex = selfIndex;
			this.il = il;
		}

		private Type selfTpe() {
			return Type.getType(LuaState.class);
		}

		public LuaState_prx push() {
			il.add(new VarInsnNode(ALOAD, selfIndex));
			return this;
		}

		public LuaState_prx do_newTable(int array, int hash) {
			push();

			il.add(ASMUtils.loadInt(array));
			il.add(ASMUtils.loadInt(hash));

			il.add(new MethodInsnNode(
					INVOKEVIRTUAL,
					selfTpe().getInternalName(),
					"newTable",
					Type.getMethodType(
							Type.getType(Table.class),
							Type.INT_TYPE,
							Type.INT_TYPE).getDescriptor(),
					false));

			return this;
		}

		public LuaState_prx call_newUpvalue() {
			il.add(new MethodInsnNode(
					INVOKEVIRTUAL,
					selfTpe().getInternalName(),
					"newUpvalue",
					Type.getMethodType(
							Type.getType(Upvalue.class),
							Type.getType(Object.class)).getDescriptor(),
					false));
			return this;
		}

	}

	private static class ObjectSink_prx {

		private final int selfIndex;
		private final InsnList il;

		public ObjectSink_prx(int selfIndex, InsnList il) {
			this.selfIndex = selfIndex;
			this.il = il;
		}

		private Type selfTpe() {
			return Type.getType(ObjectSink.class);
		}

		public ObjectSink_prx push() {
			il.add(new VarInsnNode(ALOAD, selfIndex));
			return this;
		}

		public ObjectSink_prx call_get(int index) {
			Check.nonNegative(index);
			if (index <= 4) {
				String methodName = "_" + index;
				il.add(new MethodInsnNode(
						INVOKEINTERFACE,
						selfTpe().getInternalName(),
						methodName,
						Type.getMethodType(
								Type.getType(Object.class)).getDescriptor(),
						true));
			}
			else {
				il.add(ASMUtils.loadInt(index));
				il.add(new MethodInsnNode(
						INVOKEINTERFACE,
						selfTpe().getInternalName(),
						"get",
						Type.getMethodType(
								Type.getType(Object.class),
								Type.INT_TYPE).getDescriptor(),
						true));
			}
			return this;
		}

		public ObjectSink_prx call_setTo(int numValues) {
			Check.nonNegative(numValues);
			if (numValues == 0) {
				il.add(new MethodInsnNode(
						INVOKEINTERFACE,
						selfTpe().getInternalName(),
						"reset",
						Type.getMethodType(
								Type.VOID_TYPE).getDescriptor(),
						true));
			}
			else {
				// TODO: determine this by reading the ObjectSink interface?
				if (numValues <= 5) {
					Type[] argTypes = new Type[numValues];
					Arrays.fill(argTypes, Type.getType(Object.class));

					il.add(new MethodInsnNode(
							INVOKEINTERFACE,
							selfTpe().getInternalName(),
							"setTo",
							Type.getMethodType(
									Type.VOID_TYPE,
									argTypes).getDescriptor(),
							true));
				}
				else {
					// TODO: iterate and push
					throw new UnsupportedOperationException("Return " + numValues + " values");
				}
			}
			return this;
		}

		public ObjectSink_prx call_tailCall(int numCallArgs) {
			Check.nonNegative(numCallArgs);
			// TODO: determine this by reading the ObjectSink interface?
			if (numCallArgs <= 4) {
				Type[] callArgTypes = new Type[numCallArgs + 1];  // don't forget the call target
				Arrays.fill(callArgTypes, Type.getType(Object.class));

				il.add(new MethodInsnNode(
						INVOKEINTERFACE,
						selfTpe().getInternalName(),
						"tailCall",
						Type.getMethodType(
								Type.VOID_TYPE,
								callArgTypes).getDescriptor(),
						true));
			}
			else {
				// TODO: iterate and push
				throw new UnsupportedOperationException("Tail call with " + numCallArgs + " arguments");
			}
			return this;
		}

	}

}

package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.PrototypeContext;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.Resumable;
import net.sandius.rembulan.core.ResumeInfo;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.TableFactory;
import net.sandius.rembulan.core.Upvalue;
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

import java.util.ArrayList;
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

	private final MethodNode methodNode;
	private final MethodNode resumeMethodNode;

	private final Map<Object, LabelNode> labels;
	private final ArrayList<LabelNode> resumptionPoints;

	private final InsnList resumeSwitch;
	private final InsnList code;
	private final InsnList errorState;
	private final InsnList resumeHandler;

	public CodeEmitter(ClassEmitter parent, PrototypeContext context) {
		this.parent = Check.notNull(parent);
		this.context = Check.notNull(context);
		this.labels = new HashMap<>();
		this.resumptionPoints = new ArrayList<>();

		this.methodNode = new MethodNode(
				ACC_PRIVATE,
				methodName(),
				methodType().getDescriptor(),
				null,
				exceptions());

		this.resumeMethodNode = new MethodNode(
				ACC_PUBLIC,
				"resume",
				Type.getMethodType(
						Type.VOID_TYPE,
						Type.getType(LuaState.class),
						Type.getType(ObjectSink.class),
						Type.getType(Object.class)).getDescriptor(),
						null,
				exceptions());

		resumeSwitch = new InsnList();
		code = new InsnList();
		errorState = new InsnList();
		resumeHandler = new InsnList();
	}

	public MethodNode node() {
		return methodNode;
	}

	public MethodNode resumeNode() {
		return resumeMethodNode;
	}

	private String methodName() {
		return "run";
	}

	private Type methodType() {
		Type[] args = new Type[3 + numOfRegisters()];
		args[0] = Type.getType(LuaState.class);
		args[1] = Type.getType(ObjectSink.class);
		args[2] = Type.INT_TYPE;
		for (int i = 3; i < args.length; i++) {
			args[i] = Type.getType(Object.class);
		}
		return Type.getMethodType(Type.VOID_TYPE, args);
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

	protected String thisClassName() {
		return context.className();
	}

	public void _note(String text) {
		System.out.println("// " + text);
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

	public void _push_int(InsnList il, int i) {
		switch (i) {
			case -1: il.add(new InsnNode(ICONST_M1)); break;
			case 0: il.add(new InsnNode(ICONST_0)); break;
			case 1: il.add(new InsnNode(ICONST_1)); break;
			case 2: il.add(new InsnNode(ICONST_2)); break;
			case 3: il.add(new InsnNode(ICONST_3)); break;
			case 4: il.add(new InsnNode(ICONST_4)); break;
			case 5: il.add(new InsnNode(ICONST_5)); break;
			default: il.add(new LdcInsnNode(i)); break;
		}
	}

	private void _push_long(InsnList il, long l) {
		if (l == 0L) il.add(new InsnNode(LCONST_0));
		else if (l == 1L) il.add(new InsnNode(LCONST_1));
		else il.add(new LdcInsnNode(l));
	}

	public void _push_double(InsnList il, double d) {
		if (d == 0.0) il.add(new InsnNode(DCONST_0));
		else if (d == 1.0) il.add(new InsnNode(DCONST_1));
		else il.add(new LdcInsnNode(d));
	}

	public void _load_k(int idx, Class castTo) {
		Object k = context.getConst(idx);

		if (k == null) {
			_push_null();
		}
		else if (k instanceof Boolean) {
			_push_int(code, (Boolean) k ? 1 : 0);
			_invokeStatic(Boolean.class, "valueOf", Type.getMethodType(Type.getType(Boolean.class), Type.BOOLEAN_TYPE));
		}
		else if (k instanceof Double || k instanceof Float) {
			_push_double(code, ((Number) k).doubleValue());
			_invokeStatic(Double.class, "valueOf", Type.getMethodType(Type.getType(Double.class), Type.DOUBLE_TYPE));
		}
		else if (k instanceof Number) {
			_push_long(code, ((Number) k).longValue());
			_invokeStatic(Long.class, "valueOf", Type.getMethodType(Type.getType(Long.class), Type.LONG_TYPE));
		}
		else if (k instanceof String) {
			code.add(new LdcInsnNode((String) k));
		}
		else {
			_note("load const #" + idx + " of type " + PrototypeContext.constantType(k));
		}

		if (castTo != null) {
			if (!castTo.isAssignableFrom(k.getClass())) {
				_checkCast(castTo);
			}
		}
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

	@Deprecated
	public static String _className(String cn) {
		return cn.replace('.', '/');
	}

	@Deprecated
	public static String _classDesc(String cn) {
		return "L" + _className(cn) + ";";
	}

	@Deprecated
	public static String _classDesc(Class clazz) {
		if (clazz.isPrimitive()) {
			if (clazz.equals(void.class)) return "V";
			else if (clazz.equals(byte.class)) return "B";
			else if (clazz.equals(boolean.class)) return "Z";
			else if (clazz.equals(char.class)) return "C";
			else if (clazz.equals(double.class)) return "D";
			else if (clazz.equals(float.class)) return "F";
			else if (clazz.equals(int.class)) return "I";
			else if (clazz.equals(long.class)) return "J";
			else if (clazz.equals(short.class)) return "S";
			else throw new IllegalArgumentException();
		}
		else {
			if (clazz.isArray()) {
				return _className(clazz.getName());
			}
			else {
				return "L" + _className(clazz.getName()) + ";";
			}
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
				_className(className),
				methodName,
				methodSignature.getDescriptor(),
				false
		));
	}

//	public void _invokeSpecial(Class clazz, String methodName, String methodSignature) {
//		_invokeSpecial(clazz.getName(), methodName, methodSignature);
//	}

	public String _methodSignature(Class returnType, Class... parameters) {
		StringBuilder bld = new StringBuilder();
		bld.append("(");
		for (Class p : parameters) {
			bld.append(_classDesc(p));
		}
		bld.append(")");
		bld.append(_classDesc(returnType));
		return bld.toString();
	}

	public void _dispatchCall(String methodName, Type signature) {
		_invokeStatic(Dispatch.class, methodName, signature);
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

	public void _checkCast(Class clazz) {
		code.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(clazz)));
	}

	public void _loadState() {
		code.add(new VarInsnNode(ALOAD, LV_STATE));
	}

	public void _loadObjectSink() {
		code.add(new VarInsnNode(ALOAD, LV_OBJECTSINK));
	}

	public void _retrieve_1() {
		_loadObjectSink();
		_invokeVirtual(ObjectSink.class, "_1", Type.getMethodType(Type.getType(Object.class)));
	}

	public void _save_pc(Object o) {
		LabelNode rl = _l(o);

		int idx = resumptionPoints.size();
		resumptionPoints.add(rl);

		_push_int(code, idx);
		code.add(new VarInsnNode(ISTORE, LV_RESUME));
	}

	public void _resumptionPoint(Object label) {
		_label_here(label);
	}

	private LabelNode l_insns_begin;
	private LabelNode l_body_begin;
	private LabelNode l_error_state;
	private LabelNode l_body_end;

	private LabelNode l_handler_begin;
	private LabelNode l_handler_end;

	public void begin() {
		l_insns_begin = new LabelNode();
		methodNode.instructions.add(l_insns_begin);

		l_body_begin = new LabelNode();
		l_error_state = new LabelNode();

		l_handler_begin = new LabelNode();
		l_handler_end = new LabelNode();

		resumptionPoints.add(l_body_begin);

		code.add(l_body_begin);
		_frame_same(code);
	}

	public void end() {
		l_body_end = new LabelNode();
		code.add(l_body_end);

		if (isResumable()) {
			_error_state();
		}
		_dispatch_table();
		if (isResumable()) {
			_resumption_handler(l_body_begin, l_body_end);
		}

		// local variable declaration

		LabelNode l_insns_end = new LabelNode();

		List<LocalVariableNode> locals = methodNode.localVariables;
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
//		methodNode.maxLocals = numOfRegisters() + 4;
//		methodNode.maxStack = numOfRegisters() + 5;

		methodNode.maxLocals = locals.size();
		methodNode.maxStack = 4 + numOfRegisters() + 5;

		methodNode.instructions.add(resumeSwitch);
		methodNode.instructions.add(code);
		methodNode.instructions.add(errorState);
		methodNode.instructions.add(resumeHandler);

		methodNode.instructions.add(l_insns_end);

		emitResumeNode();
	}

	private void emitResumeNode() {
//		if (isResumable()) {
//
//		}
//		else
		{
			InsnList il = resumeMethodNode.instructions;
			List<LocalVariableNode> locals = resumeMethodNode.localVariables;

			LabelNode begin = new LabelNode();
			LabelNode end = new LabelNode();

			il.add(begin);
			il.add(new TypeInsnNode(NEW, Type.getInternalName(UnsupportedOperationException.class)));
			il.add(new InsnNode(DUP));
			il.add(new MethodInsnNode(
					INVOKESPECIAL,
					Type.getInternalName(UnsupportedOperationException.class),
					"<init>",
					Type.getMethodType(
							Type.VOID_TYPE).getDescriptor(),
					false));
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
		errorState.add(new MethodInsnNode(
				INVOKESPECIAL,
				Type.getInternalName(IllegalStateException.class),
				"<init>",
				Type.getMethodDescriptor(Type.VOID_TYPE),
				false));
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

	protected void _make_saved_state(InsnList il) {
		il.add(new TypeInsnNode(NEW, Type.getInternalName(ResumeInfo.SavedState.class)));
		il.add(new InsnNode(DUP));

		// resumption point
		il.add(new VarInsnNode(ILOAD, LV_RESUME));

		// registers
		int numRegs = numOfRegisters();
		_push_int(il, numRegs);
		il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
		for (int i = 0; i < numRegs; i++) {
			il.add(new InsnNode(DUP));
			_push_int(il, i);
			il.add(new VarInsnNode(ALOAD, REGISTER_OFFSET + i));
			il.add(new InsnNode(AASTORE));
		}

		il.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(ResumeInfo.SavedState.class), "<init>", Type.getMethodType(Type.VOID_TYPE, Type.INT_TYPE, ASMUtils.arrayTypeFor(Object.class)).getDescriptor(), false));
//		_ctor(Type.getType(ResumeInfo.SavedState.class), Type.INT_TYPE, ASMUtils.arrayTypeFor(Object.class));
	}

	protected void _resumption_handler(LabelNode begin, LabelNode end) {
		resumeHandler.add(l_handler_begin);
		resumeHandler.add(new FrameNode(F_SAME1, 0, null, 1, new Object[] { Type.getInternalName(ControlThrowable.class) }));

		resumeHandler.add(new InsnNode(DUP));
//		// TODO: is this required? maybe we could do all this on stack -- we'd simply DUP the exception here
//		resumeHandler.add(new VarInsnNode(ASTORE, REGISTER_OFFSET + numOfRegisters()));
//		resumeHandler.add(new VarInsnNode(ALOAD, REGISTER_OFFSET + numOfRegisters()));

		resumeHandler.add(new TypeInsnNode(NEW, Type.getInternalName(ResumeInfo.class)));
		resumeHandler.add(new InsnNode(DUP));

		resumeHandler.add(new VarInsnNode(ALOAD, 0));
//		resumeHandler.add(new TypeInsnNode(CHECKCAST, Type.getDescriptor(Resumable.class)));  // FIXME: get rid of this
		_make_saved_state(resumeHandler);

		resumeHandler.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(ResumeInfo.class), "<init>", Type.getMethodType(Type.VOID_TYPE, Type.getType(Resumable.class), Type.getType(Object.class)).getDescriptor(), false));
		resumeHandler.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(ControlThrowable.class), "push", Type.getMethodType(Type.VOID_TYPE, Type.getType(ResumeInfo.class)).getDescriptor(), false));

//		// TODO: remove if not actually needed (maybe we could do all of this on stack)
//		resumeHandler.add(new VarInsnNode(ALOAD, REGISTER_OFFSET + numOfRegisters()));

		// rethrow
		resumeHandler.add(new InsnNode(ATHROW));

		resumeHandler.add(l_handler_end);

		methodNode.tryCatchBlocks.add(new TryCatchBlockNode(l_insns_begin, l_error_state, l_handler_begin, Type.getInternalName(ControlThrowable.class)));
		methodNode.tryCatchBlocks.add(new TryCatchBlockNode(l_error_state, l_handler_begin, l_handler_begin, Type.getInternalName(ControlThrowable.class)));

//		methodNode.tryCatchBlocks.add(new TryCatchBlockNode(begin, end, l_handler_begin, Type.getInternalName(ControlThrowable.class)));
	}

	public void _get_upvalue_ref(int idx) {
		code.add(new VarInsnNode(ALOAD, 0));
		code.add(new FieldInsnNode(
				GETFIELD,
				_className(thisClassName()),
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
		code.add(new TypeInsnNode(NEW, _className(className)));
	}

	public void _new(Class clazz) {
		_new(clazz.getName());
	}

	public void _ctor(Type clazz, Type... args) {
		_invokeSpecial(clazz.getInternalName(), "<init>", Type.getMethodType(Type.VOID_TYPE, args));
	}

	public void _ctor(String className, Class... args) {
		Type[] argTypes = new Type[args.length];
		for (int i = 0; i < args.length; i++) {
			argTypes[i] = Type.getType(args[i]);
		}
		_ctor(Type.getType(_classDesc(className)), argTypes);
	}

	public void _ctor(Class clazz, Class... args) {
		_ctor(clazz.getName(), args);
	}

	public void _capture(int idx) {
		_new(Upvalue.class);
		_dup();
		_load_reg_value(idx);
		_ctor(Upvalue.class, Object.class);
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
		_loadState();
		_invokeVirtual(LuaState.class, "tableFactory", Type.getMethodType(Type.getType(TableFactory.class)));
		_push_int(code, array);
		_push_int(code, hash);
		_invokeVirtual(TableFactory.class, "newTable", Type.getMethodType(Type.getType(Table.class), Type.INT_TYPE, Type.INT_TYPE));
	}

	public void _if_null(Object target) {
		code.add(new JumpInsnNode(IFNULL, _l(target)));
	}

	public void _if_nonnull(Object target) {
		code.add(new JumpInsnNode(IFNONNULL, _l(target)));
	}

	public void _tailcall_0() {
		_invokeInterface(ObjectSink.class, "tailCall", Type.getMethodType(Type.VOID_TYPE));
	}

	public void _tailcall_1() {
		Type o = Type.getType(Object.class);
		_invokeInterface(ObjectSink.class, "tailCall", Type.getMethodType(Type.VOID_TYPE, o));
	}

	public void _tailcall_2() {
		Type o = Type.getType(Object.class);
		_invokeInterface(ObjectSink.class, "tailCall", Type.getMethodType(Type.VOID_TYPE, o, o));
	}

	public void _tailcall_3() {
		Type o = Type.getType(Object.class);
		_invokeInterface(ObjectSink.class, "tailCall", Type.getMethodType(Type.VOID_TYPE, o, o, o));
	}

	public void _setret_0() {
		_invokeInterface(ObjectSink.class, "reset", Type.getMethodType(Type.VOID_TYPE));
	}

	public void _setret_1() {
		Type o = Type.getType(Object.class);
		_invokeInterface(ObjectSink.class, "setTo", Type.getMethodType(Type.VOID_TYPE, o));
	}

	public void _setret_2() {
		Type o = Type.getType(Object.class);
		_invokeInterface(ObjectSink.class, "setTo", Type.getMethodType(Type.VOID_TYPE, o, o));
	}

	public void _setret_3() {
		Type o = Type.getType(Object.class);
		_invokeInterface(ObjectSink.class, "setTo", Type.getMethodType(Type.VOID_TYPE, o, o));
	}

	public void _line_here(int line) {
		LabelNode l = _l(new Object());
		code.add(l);
		code.add(new LineNumberNode(line, l));
	}

}

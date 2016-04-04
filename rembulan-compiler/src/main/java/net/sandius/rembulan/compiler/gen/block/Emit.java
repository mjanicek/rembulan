package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.PrototypeContext;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.TableFactory;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

public class Emit {

	public final int REGISTER_OFFSET = 3;

	private final PrototypeContext context;
	private final MethodVisitor visitor;

	private final Map<Object, Label> labels;

	public Emit(PrototypeContext context, MethodVisitor visitor) {
		this.context = Check.notNull(context);
		this.visitor = Check.notNull(visitor);
		this.labels = new HashMap<>();
	}

	protected Label _l(Object key) {
		Label l = labels.get(key);

		if (l != null) {
			return l;
		}
		else {
			Label nl = new Label();
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
		visitor.visitInsn(Opcodes.DUP);
	}

	public void _swap() {
		visitor.visitInsn(Opcodes.SWAP);
	}

	public void _push_null() {
		visitor.visitInsn(Opcodes.ACONST_NULL);
	}

	public void _push_int(int i) {
		switch (i) {
			case -1: visitor.visitInsn(Opcodes.ICONST_M1); break;
			case 0: visitor.visitInsn(Opcodes.ICONST_0); break;
			case 1: visitor.visitInsn(Opcodes.ICONST_1); break;
			case 2: visitor.visitInsn(Opcodes.ICONST_2); break;
			case 3: visitor.visitInsn(Opcodes.ICONST_3); break;
			case 4: visitor.visitInsn(Opcodes.ICONST_4); break;
			case 5: visitor.visitInsn(Opcodes.ICONST_5); break;
			default: visitor.visitLdcInsn(i); break;
		}
	}

	private void _push_long(long l) {
		if (l == 0L) visitor.visitInsn(Opcodes.LCONST_0);
		else if (l == 1L) visitor.visitInsn(Opcodes.LCONST_1);
		else visitor.visitLdcInsn(l);
	}

	public void _push_double(double d) {
		if (d == 0.0) visitor.visitInsn(Opcodes.DCONST_0);
		else if (d == 1.0) visitor.visitInsn(Opcodes.DCONST_1);
		else visitor.visitLdcInsn(d);
	}

	public void _load_k(int idx, Class castTo) {
		Object k = context.getConst(idx);

		if (k == null) {
			_push_null();
		}
		else if (k instanceof Boolean) {
			_push_int((Boolean) k ? 1 : 0);
			_invokeStatic(Boolean.class, "valueOf", Type.getMethodType(Type.getType(Boolean.class), Type.BOOLEAN_TYPE));
		}
		else if (k instanceof Double || k instanceof Float) {
			_push_double(((Number) k).doubleValue());
			_invokeStatic(Double.class, "valueOf", Type.getMethodType(Type.getType(Double.class), Type.DOUBLE_TYPE));
		}
		else if (k instanceof Number) {
			_push_long(((Number) k).longValue());
			_invokeStatic(Long.class, "valueOf", Type.getMethodType(Type.getType(Long.class), Type.LONG_TYPE));
		}
		else if (k instanceof String) {
			visitor.visitLdcInsn((String) k);
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
		visitor.visitVarInsn(Opcodes.ALOAD, REGISTER_OFFSET + idx);
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
		visitor.visitVarInsn(Opcodes.ALOAD, REGISTER_OFFSET + idx);
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
		visitor.visitVarInsn(Opcodes.ASTORE, REGISTER_OFFSET + r);
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
	public String _className(String cn) {
		return cn.replace('.', '/');
	}

	@Deprecated
	public String _classDesc(Class clazz) {
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
		visitor.visitMethodInsn(
				Opcodes.INVOKESTATIC,
				Type.getInternalName(clazz),
				methodName,
				methodSignature.getDescriptor(),
				false
		);
	}

	public void _invokeVirtual(Class clazz, String methodName, Type methodSignature) {
		visitor.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL,
				Type.getInternalName(clazz),
				methodName,
				methodSignature.getDescriptor(),
				false
		);
	}

	public void _invokeInterface(Class clazz, String methodName, Type methodSignature) {
		visitor.visitMethodInsn(
				Opcodes.INVOKEINTERFACE,
				Type.getInternalName(clazz),
				methodName,
				methodSignature.getDescriptor(),
				true
		);
	}

	public void _invokeSpecial(String className, String methodName, Type methodSignature) {
		visitor.visitMethodInsn(
				Opcodes.INVOKESPECIAL,
				_className(className),
				methodName,
				methodSignature.getDescriptor(),
				false
		);
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
		visitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(clazz));
	}

	public void _loadState() {
		visitor.visitVarInsn(Opcodes.ALOAD, 0);
	}

	public void _loadObjectSink() {
		visitor.visitVarInsn(Opcodes.ALOAD, 1);
	}

	public void _retrieve_1() {
		_invokeVirtual(ObjectSink.class, "_1", Type.getMethodType(Type.getType(Object.class)));
	}

	public void _save_pc(Object o) {
		_note("save pc, resumption point is " + o.toString());
	}

	public void _resumptionPoint(Object label) {
		_label_here(label);
	}

	public String _upvalue_field_name(int idx) {
		String n = context.upvalueName(idx);
		if (n != null) {
			return n;  // FIXME: make sure it's a valid name, & that it's unique!
		}
		else {
			return "uv_" + idx;
		}
	}

	public void _get_upvalue_ref(int idx) {
		visitor.visitFieldInsn(
				Opcodes.GETFIELD,
				_className(thisClassName()),
				_upvalue_field_name(idx),
				Type.getDescriptor(Upvalue.class));
	}

	public void _get_upvalue_value() {
		_invokeVirtual(Upvalue.class, "get", Type.getMethodType(Type.getType(Object.class)));
	}

	public void _set_upvalue_value() {
		_invokeVirtual(Upvalue.class, "set", Type.getMethodType(Type.VOID_TYPE, Type.getType(Object.class)));
	}

	public void _return() {
		visitor.visitInsn(Opcodes.RETURN);
	}

	public void _new(String className) {
		visitor.visitTypeInsn(Opcodes.NEW, _className(className));
	}

	public void _new(Class clazz) {
		_new(clazz.getName());
	}

	public void _ctor(String className, Class... args) {
		Type[] argTypes = new Type[args.length];
		for (int i = 0; i < args.length; i++) {
			argTypes[i] = Type.getType(args[i]);
		}
		_invokeSpecial(className, "<init>", Type.getMethodType(Type.VOID_TYPE, argTypes));
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

	public void _label_here(Object identity) {
		Label l = _l(identity);
		visitor.visitLabel(l);
		visitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
	}

	public void _goto(Object l) {
		visitor.visitJumpInsn(Opcodes.GOTO, _l(l));
	}

	public void _next_insn(Target t) {
		if (t.inSize() < 2) {
			// can be inlined, TODO: check this again
			_note("goto ignored: " + t.toString());
		}
		else {
			_goto(t);
		}
	}

	public void _new_table(int array, int hash) {
		_loadState();
		_invokeVirtual(LuaState.class, "tableFactory", Type.getMethodType(Type.getType(TableFactory.class)));
		_push_int(array);
		_push_int(hash);
		_invokeVirtual(TableFactory.class, "newTable", Type.getMethodType(Type.getType(Table.class), Type.INT_TYPE, Type.INT_TYPE));
	}

	public void _if_null(Object target) {
		visitor.visitJumpInsn(Opcodes.IFNULL, _l(target));
	}

	public void _if_nonnull(Object target) {
		visitor.visitJumpInsn(Opcodes.IFNONNULL, _l(target));
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
		Label l = _l(new Object());
		visitor.visitLabel(l);
		visitor.visitLineNumber(line, l);
	}

}

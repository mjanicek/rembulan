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

public class Emit {

	public final int REGISTER_OFFSET = 3;

	private final PrototypeContext context;

	public Emit(PrototypeContext context) {
		this.context = Check.notNull(context);
	}

	protected String thisClassName() {
		return context.className();
	}

	public void _note(String text) {
		System.out.println("// " + text);
	}

	public void _dup() {
		System.out.println("  DUP");
	}

	public void _swap() {
		System.out.println("  SWAP");
	}

	public void _push_null() {
		System.out.println("  CONST_NULL");
	}

	public void _push_int(int i) {
		System.out.println("  ICONST_" + i);
	}

	public void _load_k(int idx, Class castTo) {
		Object k = context.getConst(idx);

		if (k == null) {
			_push_null();
		}
		else if (k instanceof Boolean) {
			Boolean bk = (Boolean) k;
			System.out.println("  ICONST_" + (bk ? "1" : "0"));
			_invokeStatic(Boolean.class, "valueOf", _methodSignature(Boolean.class, boolean.class));
		}
		else if (k instanceof Double || k instanceof Float) {
			System.out.println("  LDC " + k.toString());
			_invokeStatic(Double.class, "valueOf", _methodSignature(Double.class, double.class));
		}
		else if (k instanceof Number) {
			System.out.println("  LDC " + k.toString());
			_invokeStatic(Long.class, "valueOf", _methodSignature(Long.class, long.class));
		}
		else if (k instanceof String) {
			String sk = (String) k;
			System.out.println("  LDC \"" + sk + "\"");
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
		System.out.println("  ALOAD " + (REGISTER_OFFSET + idx));
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
		System.out.println("  ALOAD " + (REGISTER_OFFSET + idx));
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
		System.out.println("  ASTORE " + (REGISTER_OFFSET + r));
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

	public String _className(String cn) {
		return cn.replace('.', '/');
	}

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

	public void _invokeStatic(Class clazz, String methodName, String methodSignature) {
		System.out.println("  INVOKESTATIC " + _className(clazz.getName()) + "." + methodName + " " + methodSignature);
	}

	public void _invokeVirtual(Class clazz, String methodName, String methodSignature) {
		System.out.println("  INVOKEVIRTUAL " + _className(clazz.getName()) + "." + methodName + " " + methodSignature);
	}

	public void _invokeInterface(Class clazz, String methodName, String methodSignature) {
		System.out.println("  INVOKEINTERFACE " + _className(clazz.getName()) + "." + methodName + " " + methodSignature);
	}

	public void _invokeSpecial(String className, String methodName, String methodSignature) {
		System.out.println("  INVOKESPECIAL " + _className(className) + "." + methodName + " " + methodSignature);
	}

	public void _invokeSpecial(Class clazz, String methodName, String methodSignature) {
		_invokeSpecial(clazz.getName(), methodName, methodSignature);
	}

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

	public void _dispatchCall(String method, String signature) {
		_invokeStatic(Dispatch.class, method, signature);
	}

	public void _checkCast(Class clazz) {
		System.out.println("  CHECKCAST " + _className(clazz.getName()));
	}

	public void _loadState() {
		System.out.println("  ALOAD 0");
	}

	public void _loadObjectSink() {
		System.out.println("  ALOAD 1");
	}

	public void _retrieve_1() {
		_invokeVirtual(ObjectSink.class, "_1", _methodSignature(Object.class));
	}

	public void _save_pc(Object o) {
		_note("save pc, resumption point is " + _asLabel(o));
	}

	public String _asLabel(Object o) {
		return "L_" + Integer.toHexString(o.hashCode()).toUpperCase();
	}

	public void _resumptionPoint(Object label) {
		System.out.println(_asLabel(label));
		System.out.println("  FRAME SAME");  // TODO
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
		String fieldFullName = _className(thisClassName()) + "." + _upvalue_field_name(idx);
		String fieldType = _classDesc(Upvalue.class);
		System.out.println("  GETFIELD " + fieldFullName + " : " + fieldType);
	}

	public void _get_upvalue_value() {
		_invokeVirtual(Upvalue.class, "get", _methodSignature(Object.class));
	}

	public void _set_upvalue_value() {
		_invokeVirtual(Upvalue.class, "set", _methodSignature(void.class, Object.class));
	}

	public void _return() {
		System.out.println("  RETURN");
	}

	public void _new(String className) {
		System.out.println("  NEW " + _className(className));
	}

	public void _new(Class clazz) {
		_new(clazz.getName());
	}

	public void _ctor(String className, Class... args) {
		_invokeSpecial(className, "<init>", _methodSignature(void.class, args));
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

	public void _label_here(Object l) {
		System.out.println(_asLabel(l));
		System.out.println("  FRAME SAME");  // TODO
	}

	public void _goto(Object l) {
		System.out.println("  GOTO " + _asLabel(l));
	}

	public void _next_insn(Target t) {
		if (t.inSize() < 2) {
			// can be inlined, TODO: check this again
			_note("goto ignored: " + _asLabel(t));
		}
		else {
			_goto(t);
		}
	}

	public void _new_table(int array, int hash) {
		_loadState();
		_invokeVirtual(LuaState.class, "tableFactory", _methodSignature(TableFactory.class));
		_push_int(array);
		_push_int(hash);
		_invokeVirtual(TableFactory.class, "newTable", _methodSignature(Table.class, int.class, int.class));
	}

	public void _if_null(Object target) {
		System.out.println("  IFNULL " + _asLabel(target));
	}

	public void _if_nonnull(Object target) {
		System.out.println("  IFNONNULL " + _asLabel(target));
	}

}

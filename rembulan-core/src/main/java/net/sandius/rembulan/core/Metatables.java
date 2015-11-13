package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public class Metatables {

	public static final String MT_ADD = "__add";
	public static final String MT_SUB = "__sub";
	public static final String MT_MUL = "__mul";
	public static final String MT_DIV = "__div";
	public static final String MT_MOD = "__mod";
	public static final String MT_POW = "__pow";
	public static final String MT_UNM = "__unm";
	public static final String MT_IDIV = "__idiv";

	public static final String MT_BAND = "__band";
	public static final String MT_BOR = "__bor";
	public static final String MT_BXOR = "__bxor";
	public static final String MT_BNOT = "__bnot";
	public static final String MT_SHL = "__shl";
	public static final String MT_SHR = "__shr";

	public static final String MT_CONCAT = "__concat";
	public static final String MT_LEN = "__len";

	public static final String MT_EQ = "__eq";
	public static final String MT_LT = "__lt";
	public static final String MT_LE = "__le";

	public static final String MT_INDEX = "__index";
	public static final String MT_NEWINDEX = "__newindex";

	public static final String MT_CALL = "__call";


	public static Table getMetatable(Object o) {
		if (o instanceof LuaObject) {
			return ((LuaObject) o).getMetatable();
		}
		else {
			LuaState lst = LuaState.getCurrentState();
			LuaType tpe = LuaType.typeOf(o);
			switch (tpe) {
				case NIL: return lst.nilMetatable();
				case BOOLEAN: return lst.booleanMetatable();
				case LIGHTUSERDATA: return lst.lightuserdataMetatable();
				case NUMBER: return lst.numberMetatable();
				case STRING: return lst.stringMetatable();
				case FUNCTION: return lst.functionMetatable();
				case THREAD: return lst.threadMetatable();
				default: throw new IllegalStateException("Illegal type: " + tpe);
			}
		}
	}

	public static Object getMetamethod(Object o, String event) {
		// o can be null
		Check.notNull(event);

		Table mt = getMetatable(o);
		if (mt != null) {
			return mt.rawget(event);
		}
		else {
			return null;
		}
	}

	public static Object binaryHandlerFor(String event, Object a, Object b) {
		Object ma = Metatables.getMetamethod(a, event);
		return ma != null ? ma : Metatables.getMetamethod(b, event);
	}

}

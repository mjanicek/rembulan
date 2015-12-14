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

	public static Table getMetatable(LuaState state, Object o) {
		Check.notNull(state);
		// o can be null

		if (o instanceof LuaObject) {
			return ((LuaObject) o).getMetatable();
		}
		else {
			LuaType tpe = LuaType.typeOf(o);
			switch (tpe) {
				case NIL: return state.nilMetatable();
				case BOOLEAN: return state.booleanMetatable();
				case LIGHTUSERDATA: return state.lightuserdataMetatable();
				case NUMBER: return state.numberMetatable();
				case STRING: return state.stringMetatable();
				case FUNCTION: return state.functionMetatable();
				case THREAD: return state.threadMetatable();
				default: throw new IllegalStateException("Illegal type: " + tpe);
			}
		}
	}

	public static Object getMetamethod(LuaState state, String event, Object o) {
		// o can be null
		Check.notNull(event);

		Table mt = getMetatable(state, o);
		if (mt != null) {
			return mt.rawget(event);
		}
		else {
			return null;
		}
	}

	@Deprecated
	public static Object getMetamethod(Object o, String event) {
		return getMetamethod(LuaState.getCurrentState(), event, o);
	}

	public static Object binaryHandlerFor(LuaState state, String event, Object a, Object b) {
		Object ma = Metatables.getMetamethod(state, event, a);
		return ma != null ? ma : Metatables.getMetamethod(state, event, b);
	}

	@Deprecated
	public static Object binaryHandlerFor(String event, Object a, Object b) {
		return binaryHandlerFor(LuaState.getCurrentState(), event, a, b);
	}

}

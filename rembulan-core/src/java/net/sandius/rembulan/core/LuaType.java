package net.sandius.rembulan.core;

public enum LuaType {

	NIL (0, "nil"),
	BOOLEAN (1, "boolean"),
	LIGHTUSERDATA (2, "lightuserdata"),
	NUMBER (3, "number"),
	STRING (4, "string"),
	TABLE (5, "table"),
	FUNCTION (6, "function"),
	USERDATA (7, "userdata"),
	THREAD (8, "thread");

	public final int ord;
	public final String name;

	LuaType(int ord, String name) {
		this.ord = ord;
		this.name = name;
	}

	public static LuaType typeOf(Object v) {
		if (v == null) return LuaType.NIL;
		else if (v instanceof Boolean) return LuaType.BOOLEAN;
		else if (v instanceof Number) return LuaType.NUMBER;
		else if (v instanceof String) return LuaType.STRING;
		else if (v instanceof Table) return LuaType.TABLE;
		else if (v instanceof Function) return LuaType.FUNCTION;
		else if (v instanceof Userdata) return LuaType.USERDATA;
		else if (v instanceof Coroutine) return LuaType.THREAD;
		else return LuaType.LIGHTUSERDATA;
	}

}

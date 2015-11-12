package net.sandius.rembulan.core;

/*
 * Mappings between types:
 *
 * Lua            Java (Rembulan)
 * --------------------------------
 * nil            null pointer
 * boolean        java.lang.Boolean
 * lightuserdata  any class other than these mentioned
 * number         java.lang.Number; java.lang.Long for integers, java.lang.Double for floats
 * string         java.lang.String
 * table          net.sandius.rembulan.core.Table
 * function       net.sandius.rembulan.core.Function
 * userdata       net.sandius.rembulan.core.Userdata
 * thread         net.sandius.rembulan.core.Coroutine
 */
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

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

}

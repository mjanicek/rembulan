package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.LuaType;

public abstract class Value {

	private Value() {
		// not to be instantiated or extended
	}

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
	 * userdata       full userdata: net.sandius.rembulan.core.Userdata
	 *                light userdata: any class other than those mentioned here
	 */
	public static LuaType typeOf(Object v) {
		if (v == null) return LuaType.NIL;
		else if (v instanceof Boolean) return LuaType.BOOLEAN;
		else if (v instanceof Number) return LuaType.NUMBER;
		else if (v instanceof String) return LuaType.STRING;
		else if (v instanceof Table) return LuaType.TABLE;
		else if (v instanceof Invokable) return LuaType.FUNCTION;
		else if (v instanceof Coroutine) return LuaType.THREAD;
		else return LuaType.USERDATA;
	}

	public static boolean isNaN(Object o) {
		return (o instanceof Double || o instanceof Float)
				&& Double.isNaN(((Number) o).doubleValue());
	}

}

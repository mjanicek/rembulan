package net.sandius.rembulan.core;

public abstract class Value {

	public static String toString(Object o) {
		if (o == null) {
			return "nil";
		}
		else {
			if (o instanceof Number) {
				return Conversions.numberToLuaFormatString((Number) o);
			}
			else {
				return o.toString();
			}
		}
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
	 * userdata       net.sandius.rembulan.core.Userdata
	 * thread         net.sandius.rembulan.core.Coroutine
	 */
	public static LuaType typeOf(Object v) {
		if (v == null) return LuaType.NIL;
		else if (v instanceof Boolean) return LuaType.BOOLEAN;
		else if (v instanceof Number) return LuaType.NUMBER;
		else if (v instanceof String) return LuaType.STRING;
		else if (v instanceof Table) return LuaType.TABLE;
		else if (v instanceof Invokable) return LuaType.FUNCTION;
		else if (v instanceof Userdata) return LuaType.USERDATA;
		else if (v instanceof Coroutine) return LuaType.THREAD;
		else return LuaType.LIGHTUSERDATA;
	}

	public static boolean isLightUserdata(Object o) {
		return Value.typeOf(o) == LuaType.LIGHTUSERDATA;
	}

}

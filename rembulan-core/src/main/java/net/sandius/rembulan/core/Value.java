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

	public static Number asNumber(Object o) {
		return o instanceof Number && (isInteger(o) || isFloat(o)) ? (Number) o : null;
	}

	public static boolean isInteger(Object o) {
		return o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte;
	}

	public static long toInteger(Object o) {
		if (!isInteger(o)) {
			throw new IllegalArgumentException("Not an integer: " + o);
		}
		return ((Number) o).longValue();
	}

	public static boolean isFloat(Object o) {
		return o instanceof Double || o instanceof Float;
	}

	public static boolean isNaN(Object o) {
		return isFloat(o) && Double.isNaN(toFloat(o));
	}

	public static double toFloat(Object o) {
		if (!isFloat(o)) {
			throw new IllegalArgumentException("Not a float: " + o);
		}
		return ((Number) o).doubleValue();
	}

	public static boolean isNumber(Object o) {
		return isInteger(o) || isFloat(o);
	}

	public static String toString(Object o) {
		if (o == null) {
			return "nil";
		}
		else {
			if (isInteger(o)) return LuaFormat.toString(toInteger(o));
			else if (isFloat(o)) return LuaFormat.toString(toFloat(o));
			else return o.toString();
		}
	}

	public static String asString(Object o) {
		return o instanceof String
				? (String) o
				: (o instanceof Number
						? (isFloat((Number) o)
								? LuaFormat.toString(((Number) o).doubleValue())
								: LuaFormat.toString(((Number) o).longValue()))
						: null);
	}

}

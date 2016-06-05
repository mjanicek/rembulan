package net.sandius.rembulan.core.legacy;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.LuaType;
import net.sandius.rembulan.core.Userdata;
import net.sandius.rembulan.core.Value;

public abstract class ValueUtils {

	public static boolean isLightUserdata(Object o) {
		return Value.typeOf(o) == LuaType.USERDATA && !(o instanceof Userdata);
	}

	public static boolean isInteger(Object o) {
		return o instanceof Number && !isFloat(o);
	}

	public static boolean isFloat(Object o) {
		return o instanceof Double || o instanceof Float;
	}

	public static boolean isNumber(Object o) {
		return isInteger(o) || isFloat(o);
	}

	public static String asString(Object o) {
		return o instanceof String
				? (String) o
				: (isInteger(o)
						? LuaFormat.toString(toInteger(o))
						: (isFloat(o)
								? LuaFormat.toString(toFloat(o))
								: null));
	}

	public static long toInteger(Object o) {
		if (!isInteger(o)) {
			throw new IllegalArgumentException("Not an integer: " + o);
		}
		return ((Number) o).longValue();
	}

	public static double toFloat(Object o) {
		if (!isFloat(o)) {
			throw new IllegalArgumentException("Not a float: " + o);
		}
		return ((Number) o).doubleValue();
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

}

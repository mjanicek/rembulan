package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.util.Check;

import java.math.BigDecimal;

/*
 * "to" conversions always succeed (they never return null),
 * "as" conversions are successful if they return a non-null result, and signal failure
 * by returning null.
 */
public abstract class Conversions {

	private Conversions() {
		// not to be instantiated
	}

	public static Long numberAsLong(Number n) {
		long l = n.longValue();
		return (double) l == n.doubleValue() && l != Long.MAX_VALUE ? l : null;
	}

	public static double stringToDouble(String s) throws NumberFormatException {
		Check.notNull(s);

		try {
			return Double.parseDouble(s);
		}
		catch (NumberFormatException e0) {
			// might be missing the trailing exponent for hex floating point constants
			try {
				return Double.parseDouble(s.trim() + "p0");
			}
			catch (NumberFormatException e1) {
				throw new NumberFormatException("Not a number: " + s);
			}
		}
	}

	public static Double stringAsDouble(String s) {
		try {
			return stringToDouble(s);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	// argument can be null
	public static Number objectAsNumber(Object o) {
		return o instanceof Number
				? (Number) o
				: o instanceof String
						? stringAsDouble((String) o)
						: null;
	}

	public static Number objectToNumber(Object o, String name) {
		Number n = objectAsNumber(o);
		if (n == null) {
			throw new IllegalArgumentException(name + " must be a number");
		}
		return n;
	}

	public static Long objectAsLong(Object o) {
		Number n = objectAsNumber(o);
		return n != null ? numberAsLong(n) : null;
	}

	public static boolean objectToBoolean(Object o) {
		return !(o == null || (o instanceof Boolean && !((Boolean) o)));
	}

	// FIXME: isn't this a coercion?
	public static String numberToString(Number n) {
		if (n instanceof Double || n instanceof Float) {
			return LuaFormat.toString(n.doubleValue());
		}
		else {
			return LuaFormat.toString(n.longValue());
		}
	}

	// FIXME: isn't this a coercion?
	public static String objectAsString(Object o) {
		return o instanceof String
				? (String) o
				: o instanceof Number
						? numberToString((Number) o)
						: null;
	}

}

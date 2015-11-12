package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

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

	public static Number stringAsNumber(String s) {
		try {
			return stringToDouble(s);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	public static String numberToLuaFormatString(Number n) {
		Check.notNull(n);

		if (RawOperators.isFloatingPoint(n)) {
			double v = n.doubleValue();
			if (Double.isNaN(v)) return "nan";
			else if (Double.isInfinite(v)) return v > 0 ? "inf" : "-inf";
			else return Double.toString(v);  // TODO: check that the format matches that of Lua
		}
		else {
			return Long.toString(n.longValue());
		}
	}

	// argument can be null
	public static Number objectAsNumber(Object o) {
		return o instanceof Number
				? (Number) o
				: o instanceof String
						? stringAsNumber((String) o)
						: null;
	}

	public static Long objectAsLong(Object o) {
		Number n = objectAsNumber(o);
		return n != null ? numberAsLong(n) : null;
	}

	public static String objectAsString(Object o) {
		return o instanceof String
				? (String) o
				: o instanceof Number
						? numberToLuaFormatString((Number) o)
						: null;
	}

	public static boolean objectToBoolean(Object o) {
		return !(o == null || (o instanceof Boolean && !((Boolean) o)));
	}

}

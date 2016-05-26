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

	public static Integer numberAsInt(Number n) {
		Long l = numberAsLong(n);
		if (l != null) {
			long ll = l;
			int i = (int) ll;
			return (long) i == ll ? i : null;
		}
		else {
			// no integer representation
			return null;
		}
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

	public static Long stringAsLong(String s) {
		try {
			return Long.parseLong(s);
		}
		catch (NumberFormatException e) {
			return null;
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

	public static Number stringAsNumber(String s) {
		Long l = stringAsLong(s);
		return l != null ? Double.valueOf(l.doubleValue()) : stringAsDouble(s);
	}

	// argument can be null
	public static Number objectAsNumber(Object o) {
		return o instanceof Number
				? (Number) o
				: o instanceof String
						? stringAsNumber((String) o)
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

	public static String objectToString(Object o) {
		String s = objectAsString(o);
		return s != null ? s : (o != null ? o.toString() : LuaFormat.NIL);
	}

	public static Object throwableToObject(Throwable ex) {
		if (ex instanceof LuaRuntimeException) {
			return ((LuaRuntimeException) ex).getErrorObject();
		}
		else {
			return ex.getMessage();
		}
	}

}

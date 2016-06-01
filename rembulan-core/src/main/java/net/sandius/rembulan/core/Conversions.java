package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaFormat;
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

	@Deprecated
	public static LInteger numberAsLong(LNumber n) {
		return n.asExactInteger();
	}

	public static Integer numberAsInt(LNumber n) {
		LInteger l = numberAsLong(n);
		if (l != null) {
			long ll = l.longValue();
			return ll >= Integer.MIN_VALUE && ll <= Integer.MAX_VALUE ? (int) ll : null;
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

	public static LNumber stringAsLNumber(String s) {
		Long l = stringAsLong(s);
		if (l != null) {
			return LInteger.valueOf(l);
		}
		else {
			Double d = stringAsDouble(s);
			return d != null ? LFloat.valueOf(d) : null;
		}
	}

	public static LNumber stringAsFloat(String s) {
		LNumber n = stringAsLNumber(s);
		return n != null ? n.toFloat() : null;
	}

	/**
	 * Converts the argument {@code o} to a number, coercing it into a Lua float
	 * if {@code o} is a string that can be converted to a number.
	 *
	 * <p>If {@code o} is already a number, returns {@code o} cast to number. If {@code o} is
	 * a string convertible to a number, it first converts the string to a number
	 * following the syntactic and lexical rules lexer, and then converts the result to
	 * a Lua float. Returns {@code null} if the argument is not a number or a string convertible
	 * to number.
	 *
	 * <p>Note that this method differs from {@link #objectAsLNumber(Object)} in that it
	 * coerces strings convertible to numbers into into floats rather than preserving
	 * their canonical representation, and also note that this conversion happens <i>after</i>
	 * the number has been parsed. Most significantly,
	 *
	 * <pre>
	 *     Conversions.objectAsFloatIfString("-0")
	 * </pre>
	 *
	 * yields {@code 0.0} rather than {@code 0} (as would be the case for {@code objectAsLNumber}),
	 * or {@code -0.0} (as it would in the case if the string was parsed directly as a float).
	 *
	 * @param o  object to convert to number, may be {@code null}
	 *
	 * @return number representing the object,
	 *         or {@code null} if {@code o} cannot be coerced into a number.
	 *
	 * @see #objectAsLNumber(Object)
	 */
	public static LNumber objectAsFloatIfString(Object o) {
		return o instanceof LNumber
				? (LNumber) o
				: o instanceof String
						? stringAsFloat((String) o)
						: null;
	}

	/**
	 * Returns the argument {@code o} as a number, coercing it into a number if it is
	 * a string convertible to number.
	 *
	 * <p>If {@code o} is already a number, returns {@code o} cast to number. If {@code o}
	 * is a string convertible to a number, it returns the number it represents (i.e.
	 * an integer if the string is a valid integral literal, or a float if it is
	 * a floating-point literal according to the syntactic and lexical rules of Lua).
	 *
	 * <p>This method differs from {@link #objectAsFloatIfString(Object)} in that it
	 * preserves the representation of coerced strings. In order to conform to Lua's
	 * conversion rules for arithmetic operations, use {@link #objectAsFloatIfString(Object)}
	 * rather than this method.
	 *
	 * @param o  object to convert to number, may be {@code null}
	 *
	 * @return number representing the object,
	 *         of {@code null} if {@code o} cannot be coerced into a number.
	 *
	 * @see #objectAsFloatIfString(Object)
	 */
	public static LNumber objectAsLNumber(Object o) {
		return o instanceof LNumber
				? (LNumber) o
				: o instanceof String
						? stringAsLNumber((String) o)
						: null;
	}

	public static LNumber objectToLNumber(Object o, String name) {
		LNumber n = objectAsLNumber(o);
		if (n == null) {
			throw new IllegalArgumentException(name + " must be a number");
		}
		return n;
	}

	public static LInteger objectAsLong(Object o) {
		LNumber n = objectAsLNumber(o);
		return n != null ? numberAsLong(n) : null;
	}

	public static boolean objectToBoolean(Object o) {
		return !(o == null || (o instanceof Boolean && !((Boolean) o)));
	}

	// FIXME: isn't this a coercion?
	@Deprecated
	public static String numberToString(LNumber n) {
		return n.toString();
	}

	// FIXME: isn't this a coercion?
	public static String objectAsString(Object o) {
		return o instanceof String || o instanceof LNumber ? o.toString() : null;
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

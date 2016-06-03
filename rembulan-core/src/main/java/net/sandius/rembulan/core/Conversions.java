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

	/**
	 * Converts the number {@code n} to a signed 64-bit integer.
	 *
	 * <p>Returns a non-{@code null} value denoting the same numeric value as {@code n},
	 * or {@code null} if {@code n} has no integer representation.
	 *
	 * @param n  number to convert to integer, must not be {@code null}
	 * @return a {@code Long} representing the number, or {@code null} if {@code n} cannot
	 *           be represented by a signed 64-bit integer.
	 */
	public static Long numberAsExactLong(Number n) {
		if (n instanceof Double || n instanceof Float) {
			double d = n.doubleValue();
			long l = (long) d;
			return (double) l == d ? l : null;
		}
		else {
			return n.longValue();
		}
	}

	/**
	 * Converts the number {@code n} to a signed 32-bit integer.
	 *
	 * <p>Returns a non-{@code null} value denoting the same numeric value as {@code n},
	 * or {@code null} if {@code n} has no integer representation.
	 *
	 * @param n  number to convert to integer, must not be {@code null}
	 * @return a {@code Integer} representing the number, or {@code null} if {@code n} cannot
	 *           be represented by a signed 32-bit integer.
	 */
	public static Integer numberAsInt(Number n) {
		Long l = numberAsExactLong(n);
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
		return l != null ? l : (Number) stringAsDouble(s);
	}

	public static Number stringAsFloat(String s) {
		Number n = stringAsNumber(s);
		return n != null ? n.doubleValue() : null;
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
	 * <p>Note that this method differs from {@link #objectAsNumber(Object)} in that it
	 * coerces strings convertible to numbers into into floats rather than preserving
	 * their canonical representation, and also note that this conversion happens <i>after</i>
	 * the number has been parsed. Most significantly,
	 *
	 * <pre>
	 *     Conversions.objectAsFloatIfString("-0")
	 * </pre>
	 *
	 * yields {@code 0.0} rather than {@code 0} (as would be the case for {@code objectAsNumber}),
	 * or {@code -0.0} (as it would in the case if the string was parsed directly as a float).
	 *
	 * @param o  object to convert to number, may be {@code null}
	 *
	 * @return number representing the object,
	 *         or {@code null} if {@code o} cannot be coerced into a number.
	 *
	 * @see #objectAsNumber(Object)
	 */
	public static Number objectAsFloatIfString(Object o) {
		return o instanceof Number
				? (Number) o
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
		return n != null ? numberAsExactLong(n) : null;
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

package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.lib.BasicLib;

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
	 *         be represented by a signed 64-bit integer
	 *
	 * @throws NullPointerException if {@code n} is {@code null}
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
	 * Converts the argument {@code o} to a number, coercing it into a Lua float
	 * if {@code o} is a string that can be converted to a number.
	 *
	 * <p>If {@code o} is already a number, returns {@code o} cast to number. If {@code o} is
	 * a string convertible to a number, it first converts the string to a number
	 * following the syntactic and lexical rules of the Lua lexer, and then converts the result to
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
	 * yields {@code 0.0} rather than {@code 0} (as would be the case with {@code objectAsNumber}),
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
		if (o instanceof Number) {
			return (Number) o;
		}
		else if (o instanceof String) {
			String s = (String) o;

			Double result;
			try {
				result = Double.valueOf((double) LuaFormat.parseInteger(s));
			}
			catch (NumberFormatException ei) {
				try {
					result = Double.valueOf(LuaFormat.parseFloat(s));
				}
				catch (NumberFormatException ef) {
					result = null;
				}
			}
			return result;
		}
		else {
			return null;
		}
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
	 *         of {@code null} if {@code o} cannot be converted to a number.
	 *
	 * @see #objectAsFloatIfString(Object)
	 */
	public static Number objectAsNumber(Object o) {
		if (o instanceof Number) {
			return (Number) o;
		}
		else if (o instanceof String) {
			String s = (String) o;

			Number result;
			try {
				result = Long.valueOf(LuaFormat.parseInteger(s));
			}
			catch (NumberFormatException ei) {
				try {
					result = Double.valueOf(LuaFormat.parseFloat(s));
				}
				catch (NumberFormatException ef) {
					result = null;
				}
			}
			return result;
		}
		else {
			return null;
		}
	}

	/**
	 * Converts the argument {@code o} to a number, throwing an {@link IllegalArgumentException}
	 * if the conversion fails.
	 *
	 * <p>The conversion rules are those of {@link #objectAsNumber(Object)}; the only difference
	 * is that this method throws an exception rather than returning {@code null} to signal errors.
	 *
	 * @param o  object to convert to number, may be {@code null}
	 * @param name  value name for error reporting, may be {@code null}
	 * @return number representing the object
	 *
	 * @throws IllegalArgumentException if {@code o} is not a number or string convertible
	 *                                  to number.
	 *
	 * @see #objectAsNumber(Object)
	 */
	public static Number objectToNumber(Object o, String name) {
		Number n = objectAsNumber(o);
		if (n == null) {
			throw new IllegalArgumentException(name + " must be a number");
		}
		else {
			return n;
		}
	}

	public static Long objectAsLong(Object o) {
		Number n = objectAsNumber(o);
		return n != null ? numberAsExactLong(n) : null;
	}

	/**
	 * Converts the argument {@code o} to boolean.
	 *
	 * <p>Returns {@code false} if and only if {@code o} is <b>nil</b> or <b>false</b>.
	 *
	 * @param o  object to convert to boolean, may be {@code null}
	 * @return {@code false} if {@code o} is <b>nil</b> or <b>false</b>, {@code true} otherwise
	 */
	public static boolean objectToBoolean(Object o) {
		return !(o == null || (o instanceof Boolean && !((Boolean) o)));
	}

	/**
	 * Returns the string representation of {@code n}.
	 *
	 * @param n  number to be converted to string, must not be {@code null}
	 * @return string representation of {@code n}
	 *
	 * @throws NullPointerException if {@code n} is {@code null}
	 */
	public static String numberToString(Number n) {
		if (n instanceof Double || n instanceof Float) {
			return LuaFormat.toString(n.doubleValue());
		}
		else {
			return LuaFormat.toString(n.longValue());
		}
	}

	/**
	 * Converts {@code o} (a string or number) to string, returning {@code null} if {@code o}
	 * does not have a string representation.
	 *
	 * @param o  object to be converted to string, may be {@code null}
	 * @return string representation of {@code o}, or {@code null} if {@code o}
	 *         is not a string or number
	 */
	public static String objectAsString(Object o) {
		return o instanceof String
				? (String) o
				: o instanceof Number
						? numberToString((Number) o)
						: null;
	}

	/**
	 * Converts the object {@code o} to a human-readable string format.
	 *
	 * <p>The conversion rules are the following:
	 *
	 * <ul>
	 *   <li>If {@code o} is a {@code string}, returns {@code o};</li>
	 *   <li>if {@code o} is a {@code number}, returns its string representation;</li>
	 *   <li>if {@code o} is <b>nil</b>, returns {@code "nil"};</li>
	 *   <li>if {@code o} is a {@code boolean}, returns {@code "true"} if {@code o} is <b>true</b>
	 *       or {@code "false"} if {@code o} is <b>false</b>;</li>
	 *   <li>otherwise, returns the string of the form {@code "TYPE: 0xHASH"}, where
	 *       TYPE is the Lua type of {@code o}, and HASH
	 *       is the {@link System#identityHashCode(Object) identity hash code} of {@code o}
	 *       in hexadecimal format.
	 *   </li>
	 * </ul>
	 *
	 * <p>Note that this method ignores the object's {@link Object#toString() toString()} method
	 * and its {@code __tostring} metamethod.
	 *
	 * @param o  the object to be converted to string, may be {@code null}
	 *
	 * @see #objectAsString(Object)
	 * @see BasicLib#_tostring()
	 */
	public static String objectToString(Object o) {
		if (o == null) return LuaFormat.NIL;
		else if (o instanceof String) return (String) o;
		else if (o instanceof Number) return numberToString((Number) o);
		else if (o instanceof Boolean) return LuaFormat.toString(((Boolean) o).booleanValue());
		else return String.format("%s: %#010x",
					PlainValueTypeNamer.INSTANCE.typeNameOf(o),
					System.identityHashCode(o));
	}

	/**
	 * Converts a {@code Throwable} {@code t} to an error object.
	 *
	 * <p>If {@code t} is a {@link LuaRuntimeException}, the result of this operation
	 * is the result of its {@link LuaRuntimeException#getErrorObject()}. Otherwise,
	 * the result is {@link Throwable#getMessage()}.
	 *
	 * @param t  throwable to convert to error object, must not be {@code null}
	 * @return error object represented by {@code t}
	 */
	public static Object throwableToErrorObject(Throwable t) {
		if (t instanceof LuaRuntimeException) {
			return ((LuaRuntimeException) t).getErrorObject();
		}
		else {
			return t.getMessage();
		}
	}

}

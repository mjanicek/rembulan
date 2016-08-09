/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	 * Returns the numerical value of the string {@code s}, or {@code null} if
	 * {@code s} does not have a numerical value.
	 *
	 * <p>If {@code s} is a valid Lua integer literal with optional sign, the numerical
	 * value is the corresponding integer; if {@code s} is a valid Lua float literal with
	 * optional sign, the numerical value is the corresponding float. Otherwise, the {@code s}
	 * does not have a numerical value.
	 *
	 * <p>Leading and trailing whitespace in {@code s} is ignored by this method.
	 *
	 * @param s  string to convert to numerical value, may be {@code null}
	 * @return number representing the numerical value of {@code s},
	 *         or {@code null} if {@code s} does not have a numerical value
	 */
	public static Number numericalValueOf(String s) {
		String trimmed = s.trim();
		try {
			return Long.valueOf(LuaFormat.parseInteger(trimmed));
		}
		catch (NumberFormatException ei) {
			try {
				return Double.valueOf(LuaFormat.parseFloat(trimmed));
			}
			catch (NumberFormatException ef) {
				return null;
			}
		}
	}

	/**
	 * Returns the numerical value of the object {@code o}, or {@code null} if {@code o}
	 * does not have a numerical value.
	 *
	 * <p>If {@code o} is already a number, returns {@code o} cast to number. If {@code o}
	 * is a string, returns its numerical value (see {@link #numericalValueOf(String)}).
	 * Otherwise, returns {@code null}.
	 *
	 * <p>This method differs from {@link #arithmeticValueOf(Object)} in that it
	 * preserves the numerical value representation of coerced strings. For use in arithmetic
	 * operations following Lua's argument conversion rules, use that method instead.
	 *
	 * @param o  object to convert to numerical value, may be {@code null}
	 * @return number representing the numerical value of {@code o},
	 *         of {@code null} if {@code o} does not have a numerical value
	 *
	 * @see #arithmeticValueOf(Object)
	 */
	public static Number numericalValueOf(Object o) {
		return o instanceof Number
				? (Number) o
				: o instanceof String
						? numericalValueOf((String) o)
						: null;
	}

	/**
	 * Returns the numerical value of {@code o}, throwing a {@link ConversionException}
	 * if {@code o} does not have a numerical value.
	 *
	 * <p>The conversion rules are those of {@link #numericalValueOf(Object)}; the only difference
	 * is that this method throws an exception rather than returning {@code null} to signal errors.
	 *
	 * @param o  object to convert to numerical value, may be {@code null}
	 * @param name  value name for error reporting, may be {@code null}
	 * @return number representing the numerical value of {@code o},
	 *         guaranteed to be non-{@code null}
	 *
	 * @throws ConversionException if {@code o} is not a number or string convertible
	 *                             to number.
	 *
	 * @see #numericalValueOf(Object)
	 */
	public static Number toNumericalValue(Object o, String name) {
		Number n = numericalValueOf(o);
		if (n == null) {
			throw new ConversionException((name != null ? name : "value") + " must be a number");
		}
		else {
			return n;
		}
	}

	/**
	 * Returns the number {@code n} in its canonical representation,
	 * i.e. a {@link java.lang.Long} if {@code n} is a Lua integer, or a {@link java.lang.Double}
	 * if {@code n} is a Lua float.
	 *
	 * @param n  number to convert to canonical representation, must not be {@code null}
	 * @return an instance of {@code Long} if {@code n} is an integer, or an instance
	 *         of {@code Double} if {@code n} is a float
	 *
	 * @throws NullPointerException if {@code n} is {@code null}
	 */
	public static Number toCanonicalNumber(Number n) {
		if (n instanceof Long) {
			// integer in its canonical representation
			return n;
		}
		else if (n instanceof Double) {
			// float in its canonical representation
			return n;
		}
		else if (n instanceof Float) {
			// re-box
			return Double.valueOf(n.doubleValue());
		}
		else {
			// re-box
			return Long.valueOf(n.longValue());
		}
	}

	/**
	 * If {@code o} is a number, returns {@code n} in its canonical representation
	 * (see {@link #toCanonicalNumber(Number)}). Otherwise, returns {@code o}.
	 *
	 * @param o  object to normalise, may be {@code null}
	 * @return an instance of {@code Long} if {@code o} is an integer,
	 *         an instance of {@code Double} if {@code o} is a float,
	 *         or {@code o} if {@code o} is not a number
	 */
	public static Object normalise(Object o) {
		return o instanceof Number ? toCanonicalNumber((Number) o) : o;
	}

	/**
	 * Returns the arithmetic value of the object {@code o}, or {@code null} if {@code o}
	 * does not have an arithmetic value.
	 *
	 * <p>If {@code o} is a number, then that number is its arithmetic value. If {@code o}
	 * is a string that has a numerical value (see {@link #numericalValueOf(String)}),
	 * its arithmetic value is the numerical value converted to a float. Otherwise,
	 * {@code o} does not have an arithmetic value.
	 *
	 * <p>Note that this method differs from {@link #numericalValueOf(Object)} in that it
	 * coerces strings convertible to numbers into into floats rather than preserving
	 * their numerical value representation, and also note that this conversion happens
	 * <i>after</i> the numerical value has been determined. Most significantly,
	 *
	 * <pre>
	 *     Conversions.arithmeticValueOf("-0")
	 * </pre>
	 *
	 * yields {@code 0.0} rather than {@code 0} (as would be the case with
	 * {@code numericalValueOf("-0")}), or {@code -0.0} (it would in the case if the string
	 * was parsed directly as a float).
	 *
	 * @param o  object to convert to arithmetic value, may be {@code null}
	 *
	 * @return number representing the arithmetic value of {@code o},
	 *         or {@code null} if {@code o} does not have an arithmetic value
	 *
	 * @see #numericalValueOf(Object)
	 */
	public static Number arithmeticValueOf(Object o) {
		if (o instanceof Number) {
			return (Number) o;
		}
		else if (o instanceof String) {
			Number n = numericalValueOf((String) o);
			return n != null ? floatValueOf(n) : null;
		}
		else {
			return null;
		}
	}

	/**
	 * Returns the integer value of the number {@code n}, or {@code null} if {@code n}
	 * does not have an integer value.
	 *
	 * <p>{@code n} has an integer value if and only if the number it denotes can be represented
	 * as a signed 64-bit integer. That integer is then the integer value of {@code n}.
	 * In other words, if {@code n} is a float, it has an integer value if and only if
	 * it can be converted to a {@code long} without loss of precision.
	 *
	 * @param n  number to convert to integer, must not be {@code null}
	 * @return a {@code Long} representing the integer value of {@code n},
	 *         or {@code null} if {@code n} does not have integer value
	 *
	 * @throws NullPointerException if {@code n} is {@code null}
	 */
	public static Long integerValueOf(Number n) {
		if (n instanceof Double || n instanceof Float) {
			double d = n.doubleValue();
			long l = (long) d;
			return (double) l == d ? Long.valueOf(l) : null;
		}
		else if (n instanceof Long) {
			return (Long) n;
		}
		else {
			return Long.valueOf(n.longValue());
		}
	}

	/**
	 * Returns the integer value of the number {@code n}, throwing
	 * a {@link NoIntegerRepresentationException} if {@code n} does not have an integer value.
	 *
	 * <p>This is a variant of {@link #integerValueOf(Number)}; the difference is that
	 * this method throws an exception rather than returning {@code null} to signal that
	 * {@code n} does not have an integer value.
	 *
	 * @param n  object to be converted to integer, must not be {@code null}
	 * @return integer value of {@code n}
	 *
	 * @throws NoIntegerRepresentationException if {@code n} does not have an integer value
	 * @throws NullPointerException if {@code n} is {@code null}
	 */
	public static long toIntegerValue(Number n) {
		Long l = integerValueOf(n);
		if (l != null) {
			return l.longValue();
		}
		else {
			throw new NoIntegerRepresentationException();
		}
	}

	/**
	 * Returns the integer value of the object {@code o}, or {@code null} if {@code o}
	 * does not have an integer value.
	 *
	 * <p>The integer value of {@code o} is the integer value of its numerical value
	 * (see {@link #numericalValueOf(Object)}), when it exists.
	 *
	 * @param o  object to be converted to integer, may be {@code null}
	 * @return a {@code Long} representing the integer value of {@code o},
	 *         or {@code null} if {@code o} does not have a integer value
	 *
	 * @see #integerValueOf(Number)
	 */
	public static Long integerValueOf(Object o) {
		Number n = numericalValueOf(o);
		return n != null ? integerValueOf(n) : null;
	}

	/**
	 * Returns the float value of the number {@code n}.
	 *
	 * <p>The float value of {@code n} is its numerical value converted to {@code double}.
	 *
	 * @param n  the number to convert to float, must not be {@code null}
	 * @return the float value of {@code n},
	 *         guaranteed to be non-{@code null}
	 *
	 * @throws NullPointerException if {@code n} is {@code null}
	 */
	public static Double floatValueOf(Number n) {
		return n instanceof Double
				? (Double) n
				: Double.valueOf(n.doubleValue());
	}

	/**
	 * Returns the boolean value of the object {@code o}.
	 *
	 * <p>The boolean value of {@code o} is {@code false} if and only if {@code o} is <b>nil</b>
	 * (i.e., {@code null}) or <b>false</b> (i.e., a {@link Boolean} {@code b} such
	 * that {@code b.booleanValue() == false}).
	 *
	 * @param o  object to convert to boolean, may be {@code null}
	 * @return {@code false} if {@code o} is <b>nil</b> or <b>false</b>, {@code true} otherwise
	 */
	public static boolean booleanValueOf(Object o) {
		return !(o == null || (o instanceof Boolean && !((Boolean) o).booleanValue()));
	}

	/**
	 * Returns the string value of the number {@code n}.
	 *
	 * <p>The string value of integers is the result of {@link LuaFormat#toString(long)}
	 * on their numerical value; similarly the string value of floats is the result
	 * of {@link LuaFormat#toString(double)} on their numerical value.
	 *
	 * @param n  number to be converted to string, must not be {@code null}
	 * @return string value of {@code n}, guaranteed to be non-{@code null}
	 *
	 * @throws NullPointerException if {@code n} is {@code null}
	 */
	public static String stringValueOf(Number n) {
		if (n instanceof Double || n instanceof Float) {
			return LuaFormat.toString(n.doubleValue());
		}
		else {
			return LuaFormat.toString(n.longValue());
		}
	}

	/**
	 * Returns the string value of the object {@code o}, or {@code null} if {@code o} does
	 * not have a string value.
	 *
	 * <p>If {@code o} is a string, that is the string value. If {@code o} is a number,
	 * returns the string value of that number (see {@link #stringValueOf(Number)}).
	 * Otherwise, {@code o} does not have a string value.
	 *
	 * @param o  object to be converted to string, may be {@code null}
	 * @return string value of {@code o}, or {@code null} if {@code o} does not have
	 *         a string value
	 */
	public static String stringValueOf(Object o) {
		return o instanceof String
				? (String) o
				: o instanceof Number
						? stringValueOf((Number) o)
						: null;
	}

	/**
	 * Converts the object {@code o} to a human-readable string format.
	 *
	 * <p>The conversion rules are the following:
	 *
	 * <ul>
	 *   <li>If {@code o} is a {@code string} or {@code number}, returns the string value
	 *       of {@code o};</li>
	 *   <li>if {@code o} is a {@code number}, returns its string value;</li>
	 *   <li>if {@code o} is <b>nil</b> (i.e., {@code null}), returns {@code "nil"};</li>
	 *   <li>if {@code o} is a {@code boolean}, returns {@code "true"} if {@code o} is <b>true</b>
	 *       or {@code "false"} if {@code o} is <b>false</b>;</li>
	 *   <li>otherwise, returns the string of the form {@code "TYPE: 0xHASH"}, where
	 *       TYPE is the Lua type of {@code o}, and HASH
	 *       is the {@link System#identityHashCode(Object) identity hash code} of {@code o}
	 *       in hexadecimal format.
	 *   </li>
	 * </ul>
	 *
	 * <p>Note that this method ignores the object's {@code toString()} method
	 * and its {@code __tostring} metamethod.
	 *
	 * @param o  the object to be converted to string, may be {@code null}
	 * @return human-readable string representation of {@code o}
	 *
	 * @see #stringValueOf(Object)
	 * @see BasicLib#_tostring()
	 */
	public static String toHumanReadableString(Object o) {
		if (o == null) return LuaFormat.NIL;
		else if (o instanceof String) return (String) o;
		else if (o instanceof Number) return stringValueOf((Number) o);
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
	 *
	 * @throws NullPointerException if {@code t} is {@code null}
	 */
	public static Object toErrorObject(Throwable t) {
		if (t instanceof LuaRuntimeException) {
			return ((LuaRuntimeException) t).getErrorObject();
		}
		else {
			return t.getMessage();
		}
	}

}

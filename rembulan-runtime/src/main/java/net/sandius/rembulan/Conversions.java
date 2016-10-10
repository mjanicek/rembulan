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

package net.sandius.rembulan;

import java.util.Arrays;

/**
 * Static methods implementing Lua value conversions.
 */
public final class Conversions {

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
	 * does not have a numerical value.</p>
	 *
	 * <p>Leading and trailing whitespace in {@code s} is ignored by this method.</p>
	 *
	 * <p>Numbers returned by this method are in the canonical representation.</p>
	 *
	 * @param s  string to convert to numerical value, may be {@code null}
	 * @return  a number representing the numerical value of {@code s} (in the canonical
	 *          representation), or {@code null} if {@code s} does not have a numerical value
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
	 * Otherwise, returns {@code null}.</p>
	 *
	 * <p>This method differs from {@link #arithmeticValueOf(Object)} in that it
	 * preserves the numerical value representation of coerced strings. For use in arithmetic
	 * operations following Lua's argument conversion rules, use that method instead.</p>
	 *
	 * <p>Numbers returned by this method are not necessarily in the canonical representation.</p>
	 *
	 * @param o  object to convert to numerical value, may be {@code null}
	 * @return number representing the numerical value of {@code o} (not necessarily in
	 *         the canonical representation), of {@code null} if {@code o} does not have
	 *         a numerical value
	 *
	 * @see #arithmeticValueOf(Object)
	 */
	public static Number numericalValueOf(Object o) {
		if (o instanceof Number) return (Number) o;
		else if (o instanceof ByteString || o instanceof String) return numericalValueOf(o.toString());
		else return null;
	}

	/**
	 * Returns the numerical value of {@code o}, throwing a {@link ConversionException}
	 * if {@code o} does not have a numerical value.
	 *
	 * <p>The conversion rules are those of {@link #numericalValueOf(Object)}; the only difference
	 * is that this method throws an exception rather than returning {@code null} to signal
	 * errors.</p>
	 *
	 * <p>Numbers returned by this method are not necessarily in the canonical representation.</p>
	 *
	 * @param o  object to convert to numerical value, may be {@code null}
	 * @param name  value name for error reporting, may be {@code null}
	 * @return number representing the numerical value of {@code o} (not necessarily in
	 *         the canonical representation), guaranteed to be non-{@code null}
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

	public static Object canonicalRepresentationOf(Object o) {
		if (o instanceof Number) return toCanonicalNumber((Number) o);
		else if (o instanceof String) return ByteString.of((String) o);
		else return o;
	}

	public static Object javaRepresentationOf(Object o) {
		if (o instanceof ByteString) return ((ByteString) o).toString();
		else return o;
	}

	public static Object[] toCanonicalValues(Object[] values) {
		for (int i = 0; i < values.length; i++) {
			Object v = values[i];
			values[i] = canonicalRepresentationOf(v);
		}
		return values;
	}

	public static Object[] toJavaValues(Object[] values) {
		for (int i = 0; i < values.length; i++) {
			Object v = values[i];
			values[i] = javaRepresentationOf(v);
		}
		return values;
	}

	public static Object[] copyAsCanonicalValues(Object[] values) {
		return toCanonicalValues(Arrays.copyOf(values, values.length));
	}

	public static Object[] copyAsJavaValues(Object[] values) {
		return toJavaValues(Arrays.copyOf(values, values.length));
	}

	/**
	 * Normalises the number {@code n} so that it may be used as a key in a Lua
	 * table.
	 *
	 * <p>If {@code n} has an integer value <i>i</i>, returns the canonical representation
	 * of <i>i</i>; otherwise, returns the canonical representation of {@code n}
	 * (see {@link #toCanonicalNumber(Number)}).</p>
	 *
	 * @param n  number to normalise, must not be {@code null}
	 * @return  an canonical integer if {@code n} has an integer value,
	 *          the canonical representation of {@code n} otherwise
	 *
	 * @throws NullPointerException if {@code n} is {@code null}
	 */
	public static Number normaliseKey(Number n) {
		Long i = integerValueOf(n);
		return i != null ? i : toCanonicalNumber(n);
	}

	/**
	 * Normalises the argument {@code o} so that it may be used safely as a key
	 * in a Lua table.
	 *
	 * <p>If {@code o} is a number, returns the number normalised (see {@link #normaliseKey(Number)}.
	 * Otherwise, returns {@code o}.</p>
	 *
	 * @param o  object to normalise, may be {@code null}
	 * @return  normalised number if {@code o} is a number, {@code o} otherwise
	 */
	public static Object normaliseKey(Object o) {
		if (o instanceof Number) return normaliseKey((Number) o);
		else if (o instanceof String) return ByteString.of((String) o);
		else return o;
	}

	/**
	 * Returns the arithmetic value of the object {@code o}, or {@code null} if {@code o}
	 * does not have an arithmetic value.
	 *
	 * <p>If {@code o} is a number, then that number is its arithmetic value. If {@code o}
	 * is a string that has a numerical value (see {@link #numericalValueOf(String)}),
	 * its arithmetic value is the numerical value converted to a float. Otherwise,
	 * {@code o} does not have an arithmetic value.</p>
	 *
	 * <p>Note that this method differs from {@link #numericalValueOf(Object)} in that it
	 * coerces strings convertible to numbers into into floats rather than preserving
	 * their numerical value representation, and also note that this conversion happens
	 * <i>after</i> the numerical value has been determined. Most significantly,</p>
	 *
	 * <pre>
	 *     Conversions.arithmeticValueOf("-0")
	 * </pre>
	 *
	 * <p>yields {@code 0.0} rather than {@code 0} (as would be the case with
	 * {@code numericalValueOf("-0")}), or {@code -0.0} (it would in the case if the string
	 * was parsed directly as a float).</p>
	 *
	 * <p>Numbers returned by this method are not necessarily in the canonical representation.</p>
	 *
	 * @param o  object to convert to arithmetic value, may be {@code null}
	 *
	 * @return number representing the arithmetic value of {@code o} (not necessarily in
	 *         the canonical representation), or {@code null} if {@code o} does not have
	 *         an arithmetic value
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
	 * it can be converted to a {@code long} without loss of precision.</p>
	 *
	 * @param n  number to convert to integer, must not be {@code null}
	 * @return a {@code Long} representing the integer value of {@code n},
	 *         or {@code null} if {@code n} does not have an integer value
	 *
	 * @throws NullPointerException if {@code n} is {@code null}
	 * @see LuaMathOperators#hasExactIntegerRepresentation(double)
	 */
	public static Long integerValueOf(Number n) {
		if (n instanceof Double || n instanceof Float) {
			double d = n.doubleValue();
			return LuaMathOperators.hasExactIntegerRepresentation(d) ? Long.valueOf((long) d) : null;
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
	 * {@code n} does not have an integer value, and that this method returns the unboxed
	 * integer value of {@code n} (as a {@code long}).</p>
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
	 * (see {@link #numericalValueOf(Object)}), when it exists.</p>
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
	 * Returns the integer value of the object {@code o}, throwing
	 * a {@link NoIntegerRepresentationException} if {@code o} does not have an integer value.
	 *
	 * <p>This is a variant of {@link #integerValueOf(Object)}; the difference is that
	 * this method throws an exception rather than returning {@code null} to signal that
	 * {@code o} does not have an integer value, and that this method returns the unboxed
	 * integer value of {@code o} (as a {@code long}).</p>
	 *
	 * @param o  object to be converted to integer, may be {@code null}
	 * @return integer value of {@code n}
	 *
	 * @throws NoIntegerRepresentationException if {@code o} does not have an integer value
	 */
	public static long toIntegerValue(Object o) {
		Long l = integerValueOf(o);
		if (l != null) {
			return l.longValue();
		}
		else {
			throw new NoIntegerRepresentationException();
		}
	}

	/**
	 * Returns the float value of the number {@code n}.
	 *
	 * <p>The float value of {@code n} is its numerical value converted to a Lua float.</p>
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
	public static ByteString stringValueOf(Number n) {
		if (n instanceof Double || n instanceof Float) {
			return ByteString.of(LuaFormat.toString(n.doubleValue()));
		}
		else {
			return ByteString.of(LuaFormat.toString(n.longValue()));
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
	public static ByteString stringValueOf(Object o) {
		if (o instanceof String) return ByteString.of((String) o);
		else if (o instanceof ByteString) return (ByteString) o;
		else if (o instanceof Number) return stringValueOf((Number) o);
		else return null;
	}

	/**
	 * Converts the object {@code o} to a human-readable string format.
	 *
	 * <p>The conversion rules are the following:
	 *
	 * <ul>
	 *   <li>If {@code o} is a {@code string} or {@code number}, returns the string value
	 *       of {@code o};</li>
	 *   <li>if {@code o} is <b>nil</b> (i.e., {@code null}), returns {@code "nil"};</li>
	 *   <li>if {@code o} is a {@code boolean}, returns {@code "true"} if {@code o} is <b>true</b>
	 *       or {@code "false"} if {@code o} is <b>false</b>;</li>
	 *   <li>otherwise, returns the string of the form {@code "TYPE: 0xHASH"}, where
	 *       TYPE is the Lua type of {@code o}, and HASH
	 *       is the {@link Object#hashCode()} of {@code o}
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
	 */
	public static String toHumanReadableString(Object o) {
		if (o == null) return LuaFormat.NIL;
		else if (o instanceof ByteString || o instanceof String) return o.toString();
		else if (o instanceof Number) return stringValueOf((Number) o).toString();
		else if (o instanceof Boolean) return LuaFormat.toString(((Boolean) o).booleanValue());
		else return String.format("%s: %#010x",
					PlainValueTypeNamer.INSTANCE.typeNameOf(o),
					o.hashCode());
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

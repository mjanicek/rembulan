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

import net.sandius.rembulan.LuaMathOperators;

/**
 * A representation of an ordering of values, allowing the comparison of values
 * of the same type (in the type parameter {@code T}).
 *
 * <p>In Lua, only strings and numbers have such an ordering. This class serves the
 * purpose of a bridge between the concrete representation of Lua numbers
 * (as {@link java.lang.Number}) and the raw operations provided by
 * {@link LuaMathOperators}, and the concrete representation of Lua strings
 * (as {@link java.lang.String}) and the comparison operations defined on them.</p>
 *
 * <p>Consequently, there are two concrete implementations of this class:
 * {@link #NUMERIC} for the numeric ordering and {@link #STRING} for the string
 * ordering. These instances may be used directly for comparing objects of known,
 * conforming types; for unknown objects, the method {@link #of(Object, Object)}
 * returns either of the two ordering instances, or {@code null} if the arguments
 * are not directly comparable in Lua.</p>
 *
 * <p>The comparison methods of this class return unboxed booleans.</p>
 *
 * <p><b>Example:</b> Given two objects {@code a}, and {@code b}, attempt to
 * evaluate the Lua expression {@code (a <= b)}:</p>
 *
 * <pre>
 *     // Object a, b
 *     final boolean result;
 *     Ordering&lt;?&gt; cmp = Ordering.of(a, b);
 *     if (cmp != null) {
 *         // a and b are comparable in cmp
 *         {@literal @}SuppressWarnings("unchecked")
 *         result = cmp.le(a, b);
 *     }
 *     else {
 *         throw new RuntimeException("a and b not comparable");
 *     }
 * </pre>
 *
 * @param <T>  the type of values comparable in the ordering
 */
public abstract class Ordering<T> {

	private Ordering() {
		// not to be instantiated by the outside world
	}

	/**
	 * Numeric ordering.
	 *
	 * Numbers are compared using the comparison methods provided by {@link LuaMathOperators},
	 * defining the ordering as one based on the ordering of the mathematical values
	 * of the numbers in question.
	 */
	public static final Ordering<Number> NUMERIC = new NumericOrdering();

	/**
	 * String ordering.
	 *
	 * Strings are compared using the method {@link String#compareTo(String)}.
	 */
	public static final Ordering<String> STRING = new StringOrdering();

	/**
	 * Returns {@code true} if {@code a} is equal to {@code b} in this ordering.
	 *
	 * @param a  first argument, must not be {@code null}
	 * @param b  second argument, must not be {@code null}
	 * @return  {@code true} iff {@code a} is equal to {@code b} in this ordering
	 *
	 * @throws NullPointerException if {@code a} or {@code b} is {@code null}
	 */
	public abstract boolean eq(T a, T b);

	/**
	 * Returns {@code true} if {@code a} is lesser than {@code b} in this ordering.
	 *
	 * @param a  first argument, must not be {@code null}
	 * @param b  second argument, must not be {@code null}
	 * @return  {@code true} iff {@code a} is lesser than {@code b} in this ordering
	 *
	 * @throws NullPointerException if {@code a} or {@code b} is {@code null}
	 */
	public abstract boolean lt(T a, T b);

	/**
	 * Returns {@code true} if {@code a} is lesser than or equal to {@code b} in this ordering.
	 *
	 * @param a  first argument, must not be {@code null}
	 * @param b  second argument, must not be {@code null}
	 * @return  {@code true} iff {@code a} is lesser than or equal to equal to {@code b}
	 *          in this ordering
	 *
	 * @throws NullPointerException if {@code a} or {@code b} is {@code null}
	 */
	public abstract boolean le(T a, T b);

	/**
	 * Returns {@code true} iff the object {@code a} is raw-equal to {@code b} following
	 * the Lua equality rules.
	 *
	 * <p>Excerpt from the Lua Reference Manual (§3.4.4):</p>
	 *
	 * <blockquote>
	 *     <p>Equality (==) first compares the type of its operands. If the types are different,
	 *     then the result is false. Otherwise, the values of the operands are compared.
	 *     Strings are compared in the obvious way. Numbers are equal if they denote the
	 *     same mathematical value.</p>
	 *
	 *     <p>Tables, userdata, and threads are compared by reference: two objects are considered
	 *     equal only if they are the same object. Every time you create a new object (a table,
	 *     userdata, or thread), this new object is different from any previously existing
	 *     object. Closures with the same reference are always equal. Closures with any
	 *     detectable difference (different behavior, different definition) are always
	 *     different.</p>
	 * </blockquote>
	 *
	 * @param a  an object, may be {@code null}
	 * @param b  another object, may be {@code null}
	 * @return  {@code true} iff {@code a} is raw-equal to {@code b}
	 */
	public static boolean isRawEqual(Object a, Object b) {
		if (a == null && b == null) {
			// two nils
			return true;
		}
		else if (a == null) {
			// b is definitely not nil; also ensures that neither a nor b is null in the tests below
			return false;
		}
		else if (a instanceof Number && b instanceof Number) {
			return Ordering.NUMERIC.eq((Number) a, (Number) b);
		}
		else if (a instanceof String && b instanceof String) {
			return Ordering.STRING.eq((String) a, (String) b);
		}
		else if (a instanceof Boolean || a instanceof Invokable) {
			// value-based equality
			return a.equals(b);
		}
		else {
			// reference-based equality
			return a == b;
		}
	}

	private static final class NumericOrdering extends Ordering<Number> {

		@Override
		public boolean eq(Number a, Number b) {
			boolean isflt_a = a instanceof Double || a instanceof Float;
			boolean isflt_b = b instanceof Double || b instanceof Float;

			if (isflt_a) {
				return isflt_b
						? LuaMathOperators.eq(a.doubleValue(), b.doubleValue())
						: LuaMathOperators.eq(a.doubleValue(), b.longValue());
			}
			else {
				return isflt_b
						? LuaMathOperators.eq(a.longValue(), b.doubleValue())
						: LuaMathOperators.eq(a.longValue(), b.longValue());
			}
		}

		@Override
		public boolean lt(Number a, Number b) {
			boolean isflt_a = a instanceof Double || a instanceof Float;
			boolean isflt_b = b instanceof Double || b instanceof Float;

			if (isflt_a) {
				return isflt_b
						? LuaMathOperators.lt(a.doubleValue(), b.doubleValue())
						: LuaMathOperators.lt(a.doubleValue(), b.longValue());
			}
			else {
				return isflt_b
						? LuaMathOperators.lt(a.longValue(), b.doubleValue())
						: LuaMathOperators.lt(a.longValue(), b.longValue());
			}
		}

		@Override
		public boolean le(Number a, Number b) {
			boolean isflt_a = a instanceof Double || a instanceof Float;
			boolean isflt_b = b instanceof Double || b instanceof Float;

			if (isflt_a) {
				return isflt_b
						? LuaMathOperators.le(a.doubleValue(), b.doubleValue())
						: LuaMathOperators.le(a.doubleValue(), b.longValue());
			}
			else {
				return isflt_b
						? LuaMathOperators.le(a.longValue(), b.doubleValue())
						: LuaMathOperators.le(a.longValue(), b.longValue());
			}
		}

	}

	private static final class StringOrdering extends Ordering<String> {

		@Override
		public boolean eq(String a, String b) {
			return a.compareTo(b) == 0;
		}

		@Override
		public boolean lt(String a, String b) {
			return a.compareTo(b) < 0;
		}

		@Override
		public boolean le(String a, String b) {
			return a.compareTo(b) <= 0;
		}

	}

	/**
	 * Based on the actual types of the arguments {@code a} and {@code b}, returns
	 * the ordering in which {@code a} and {@code b} can be compared, or {@code null}
	 * if they are not comparable.
	 *
	 * <p>More specifically, if {@code a} and {@code b} are both numbers, returns
	 * {@link #NUMERIC}; if {@code a} and {@code b} are both strings, returns
	 * {@link #STRING}; otherwise, returns {@code null}.</p>
	 *
	 * <p>Note that when the result is non-{@code null}, it is guaranteed that
	 * 1) neither {@code a} nor {@code b} is {@code null}; and 2)
	 * both {@code a} and {@code b} are of the accepted type of the comparison methods
	 * (i.e. have been checked at runtime to be subclasses of the erased type parameter
	 * {@code T} of the returned ordering). The following construction is therefore valid:</p>
	 *
	 * <pre>
	 *     // Object a, b
	 *     Ordering&lt;?&gt; cmp = Ordering.of(a, b);
	 *     if (cmp != null) {
	 *         // a and b are subclasses of cmp's (erased) type parameter,
	 *         // so it is okay to suppress the warning here:
	 *         {@literal @}SuppressWarnings("unchecked")
	 *         boolean result = cmp.lt(a, b);
	 *     }
	 * </pre>
	 *
	 * @param a  an object, may be {@code null}
	 * @param b  another object, may be {@code null}
	 * @return  {@link #NUMERIC} if both {@code a} and {@code b} are numbers;
	 *          {@link #STRING} if both {@code a} and {@code b} are strings;
	 *          {@code null} otherwise
	 */
	public static Ordering<?> of(Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			return NUMERIC;
		}
		else if (a instanceof String && b instanceof String) {
			return STRING;
		}
		else {
			return null;
		}
	}

}

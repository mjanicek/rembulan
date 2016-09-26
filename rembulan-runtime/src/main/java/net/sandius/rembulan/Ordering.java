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

import net.sandius.rembulan.runtime.LuaFunction;

import java.util.Comparator;

/**
 * A representation of an ordering on values, allowing the comparison of values
 * of the same type (in the type parameter {@code T}).
 *
 * <p>In Lua, only strings and numbers have such an ordering. This class serves the
 * purpose of a bridge between the concrete representation of Lua numbers
 * (as {@link java.lang.Number}) and the raw comparison operations provided by
 * {@link LuaMathOperators}, and the concrete representation of Lua strings
 * (as {@link java.lang.String}) and the comparison operations defined on them.</p>
 *
 * <p>Consequently, there are two concrete implementations of this class:
 * {@link #NUMERIC} for the numeric ordering and {@link #STRING} for the string
 * ordering. These instances may be used directly for comparing objects of known,
 * conforming types; for unknown objects, the method {@link #of(Object, Object)}
 * returns an ordering that accepts {@link java.lang.Object}, and uses one of
 * the two ordering instances, or {@code null} if the arguments
 * are not directly comparable in Lua.</p>
 *
 * <p>The comparison methods of this class return unboxed booleans.</p>
 *
 * <p>This class implements the {@link Comparator} interface by imposing a total
 * order on the accepted values. For numbers, this total ordering is <i>different</i>
 * from the one imposed by this class. See the documentation of {@link NumericOrdering}
 * for more details.</p>
 *
 * <p><b>Example:</b> Given two objects {@code a}, and {@code b}, attempt to
 * evaluate the Lua expression {@code (a <= b)}:</p>
 *
 * <pre>
 *     // Object a, b
 *     final boolean result;
 *     Ordering&lt;Object&gt; cmp = Ordering.of(a, b);
 *     if (cmp != null) {
 *         // a and b are comparable in cmp
 *         result = cmp.le(a, b);
 *     }
 *     else {
 *         throw new RuntimeException("a and b not comparable");
 *     }
 * </pre>
 *
 * @param <T>  the type of values comparable in the ordering
 */
public abstract class Ordering<T> implements Comparator<T> {

	private Ordering() {
		// not to be instantiated by the outside world
	}

	/**
	 * A static instance of the numeric ordering.
	 */
	public static final NumericOrdering NUMERIC = new NumericOrdering();

	/**
	 * A static instance of the string ordering.
	 */
	public static final StringOrdering STRING = new StringOrdering();

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
		else if (a instanceof Boolean || a instanceof LuaFunction) {
			// value-based equality
			return a.equals(b);
		}
		else {
			// reference-based equality
			return a == b;
		}
	}

	/**
	 * Numeric ordering.
	 *
	 * <p>Numbers are compared using the comparison methods provided by {@link LuaMathOperators},
	 * defining the ordering as one based on the ordering of the mathematical values
	 * of the numbers in question.</p>
	 *
	 * <p>This class implements the {@link Comparator} interface by imposing a total order
	 * on numbers that differs from the ordering defined by the methods
	 * {@link #eq(Number, Number)}, {@link #lt(Number, Number)}
	 * and {@link #le(Number, Number)}:</p>
	 *
	 * <ul>
	 *     <li><i>NaN</i> is treated as equal to itself and greater than any other
	 *     number, while {@code eq(a, b) == false} and {@code lt(a, b) == false}
	 *     when {@code a} or {@code b} is <i>NaN</i>;
	 *     <li>{@code -0.0} is considered to be lesser than {@code 0.0},
	 *     while {@code eq(-0.0, 0.0) == true} and {@code lt(-0.0, 0.0) == false}.</li>
	 * </ul>
	 *
	 * <p>Note that the total ordering imposed by the {@link #compare(Number, Number)}
	 * is <i>inconsistent with equals</i>.</p>
	 *
	 * <p>For proper treatment of <i>NaN</i>s and (float) zero values, use the
	 * {@code Ordering} methods directly.</p>
	 *
	 */
	public static final class NumericOrdering extends Ordering<Number> {

		private NumericOrdering() {
			// not to be instantiated by the outside world
		}

		/**
		 * Returns {@code true} iff {@code a} denotes the same mathematical value
		 * as {@code b}.
		 *
		 * <p>Note that since <i>NaN</i> does not denote any mathematical value,
		 * this method returns {@code false} whenever any of its arguments is <i>NaN</i>.</p>
		 *
		 * @param a  first argument, must not be {@code null}
		 * @param b  second argument, must not be {@code null}
		 * @return  {@code true} iff {@code a} and {@code b} denote the same mathematical
		 *          value
		 *
		 * @throws NullPointerException  if {@code a} or {@code b} is {@code null}
		 */
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

		/**
		 * Returns {@code true} iff the mathematical value denoted by {@code a}
		 * is lesser than the mathematical value denoted by {@code b}.
		 *
		 * <p>Note that since <i>NaN</i> does not denote any mathematical value,
		 * this method returns {@code false} whenever any of its arguments is <i>NaN</i>.</p>
		 *
		 * @param a  first argument, must not be {@code null}
		 * @param b  second argument, must not be {@code null}
		 * @return  {@code true} iff the mathematical value denoted by {@code a}
		 *          is lesser than the mathematical value denoted by {@code b}
		 *
		 * @throws NullPointerException  if {@code a} or {@code b} is {@code null}
		 */
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

		/**
		 * Returns {@code true} iff the mathematical value denoted by {@code a}
		 * is lesser than or equal to the mathematical value denoted by {@code b}.
		 *
		 * <p>Note that since <i>NaN</i> does not denote any mathematical value,
		 * this method returns {@code false} whenever any of its arguments is <i>NaN</i>.</p>
		 *
		 * @param a  first argument, must not be {@code null}
		 * @param b  second argument, must not be {@code null}
		 * @return  {@code true} iff the mathematical value denoted by {@code a}
		 *          is lesser than or equal to the mathematical value denoted
		 *          by {@code b}
		 *
		 * @throws NullPointerException  if {@code a} or {@code b} is {@code null}
		 */
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

		/**
		 * Compare the numbers {@code a} and {@code b}, yielding an integer that
		 * is negative, zero or positive if {@code a} is lesser than, equal to, or greater
		 * than {@code b}.
		 *
		 * <p>The ordering imposed by this method differs from the one defined
		 * by the methods {@link #eq(Number, Number)}, {@link #lt(Number, Number)}
		 * and {@link #le(Number, Number)} in the treatment of <i>NaN</i>s
		 * and float zeros:</p>
		 *
		 * <ul>
		 *     <li><i>NaN</i> is treated as equal to itself and greater than any other
		 *     number, while {@code eq(a, b) == false} and {@code lt(a, b) == false}
		 *     when {@code a} or {@code b} is <i>NaN</i>;
		 *     <li>{@code -0.0} is considered to be lesser than {@code 0.0},
		 *     while {@code eq(-0.0, 0.0) == true} and {@code lt(-0.0, 0.0) == false}.</li>
		 * </ul>
		 *
		 * <p>The total ordering of {@code Number} objects imposed by this method
		 * is <i>inconsistent with equals</i>.</p>
		 *
		 * @param a  first argument, must not be {@code null}
		 * @param b  second argument, must not be {@code null}
		 * @return  a negative, zero or positive integer if the number {@code a} is lesser
		 *          than, equal to, or greater than the number {@code b}
		 *
		 * @throws NullPointerException  if {@code a} or {@code b} is {@code null}
		 */
		@Override
		public int compare(Number a, Number b) {
			if (lt(a, b)) {
				return -1;
			}
			else if (lt(b, a)) {
				return 1;
			}
			else {
				// treat NaN as equal to itself and greater than any other number,
				// and -0.0 as lesser than 0.0
				return Double.compare(a.doubleValue(), b.doubleValue());
			}
		}

	}

	/**
	 * String ordering.
	 *
	 * <p>This is the (total) lexicographical ordering imposed by the method
	 * {@link String#compareTo(String)}.</p>
	 */
	public static final class StringOrdering extends Ordering<String> {

		private StringOrdering() {
			// not to be instantiated by the outside world
		}

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

		@Override
		public int compare(String a, String b) {
			return a.compareTo(b);
		}

	}

	private static final NumericObjectOrdering NUMERIC_OBJECT = new NumericObjectOrdering();
	private static final StringObjectOrdering STRING_OBJECT = new StringObjectOrdering();

	private static class NumericObjectOrdering extends Ordering<Object> {

		@Override
		public boolean eq(Object a, Object b) {
			return NUMERIC.eq((Number) a, (Number) b);
		}

		@Override
		public boolean lt(Object a, Object b) {
			return NUMERIC.lt((Number) a, (Number) b);
		}

		@Override
		public boolean le(Object a, Object b) {
			return NUMERIC.le((Number) a, (Number) b);
		}

		@Override
		public int compare(Object a, Object b) {
			return NUMERIC.compare((Number) a, (Number) b);
		}

	}

	private static class StringObjectOrdering extends Ordering<Object> {

		@Override
		public boolean eq(Object a, Object b) {
			return STRING.eq((String) a, (String) b);
		}

		@Override
		public boolean lt(Object a, Object b) {
			return STRING.lt((String) a, (String) b);
		}

		@Override
		public boolean le(Object a, Object b) {
			return STRING.le((String) a, (String) b);
		}

		@Override
		public int compare(Object a, Object b) {
			return STRING.compare((String) a, (String) b);
		}

	}

	/**
	 * Based on the actual types of the arguments {@code a} and {@code b}, returns
	 * the ordering in which {@code a} and {@code b} can be compared, or {@code null}
	 * if they are not comparable.
	 *
	 * <p>More specifically, if {@code a} and {@code b} are both numbers, returns
	 * an ordering that uses (but is distinct from) {@link #NUMERIC}; if {@code a} and
	 * {@code b} are both strings, returns an ordering that uses (but is distinct from)
	 * {@link #STRING}; otherwise, returns {@code null}.</p>
	 *
	 * <p>Note that when the result is non-{@code null}, it is guaranteed that
	 * 1) neither {@code a} nor {@code b} is {@code null}; and 2)
	 * both {@code a} and {@code b} are of types accepted by the underlying ordering.
	 * Caution must be observed when using the ordering with another object {@code c}
	 * (i.e., other than {@code a} or {@code b}): the returned ordering will throw
	 * a {@link ClassCastException} if {@code c} is of an incompatible type, or
	 * a {@link NullPointerException} if {@code c} is {@code null}.</p>
	 *
	 * @param a  an object, may be {@code null}
	 * @param b  another object, may be {@code null}
	 * @return  an ordering based on {@link #NUMERIC} if both {@code a} and {@code b} are numbers;
	 *          an ordering based on {@link #STRING} if both {@code a} and {@code b} are strings;
	 *          {@code null} otherwise
	 */
	public static Ordering<Object> of(Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			return NUMERIC_OBJECT;
		}
		else if (a instanceof String && b instanceof String) {
			return STRING_OBJECT;
		}
		else {
			return null;
		}
	}

}

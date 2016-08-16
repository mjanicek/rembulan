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

public abstract class ComparisonImplementation {

	public static final NumericComparisonImplementation NUMERIC_CMP = new NumericComparisonImplementation();
	public static final StringComparisonImplementation STRING_CMP = new StringComparisonImplementation();

	public abstract boolean do_eq(Object a, Object b);
	public abstract boolean do_lt(Object a, Object b);
	public abstract boolean do_le(Object a, Object b);

	public static boolean eq(Object a, Object b) {
		if (a == null && b == null) {
			// two nils
			return true;
		}
		else if (a == null) {
			// b is definitely not nil; also ensures that neither a nor b is null in the tests below
			return false;
		}
		else if (a instanceof Number && b instanceof Number) {
			return numericEq((Number) a, (Number) b);
		}
		else if (a instanceof Boolean || a instanceof String || a instanceof Invokable) {
			// value-based equality
			return a.equals(b);
		}
		else {
			// reference-based equality
			return a == b;
		}
	}

	public static boolean numericEq(Number a, Number b) {
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

	public static boolean numericLt(Number a, Number b) {
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

	public static boolean numericLe(Number a, Number b) {
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

	public static class NumericComparisonImplementation extends ComparisonImplementation {

		@Override
		public boolean do_eq(Object a, Object b) {
			return numericEq((Number) a, (Number) b);
		}

		@Override
		public boolean do_lt(Object a, Object b) {
			return numericLt((Number) a, (Number) b);
		}

		@Override
		public boolean do_le(Object a, Object b) {
			return numericLe((Number) a, (Number) b);
		}

	}

	public static class StringComparisonImplementation extends ComparisonImplementation {

		@Override
		public boolean do_eq(Object a, Object b) {
			return ((String) a).compareTo((String) b) == 0;
		}

		@Override
		public boolean do_lt(Object a, Object b) {
			return ((String) a).compareTo((String) b) < 0;
		}

		@Override
		public boolean do_le(Object a, Object b) {
			return ((String) a).compareTo((String) b) <= 0;
		}

	}

	public static ComparisonImplementation of(Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			return NUMERIC_CMP;
		}
		else if (a instanceof String && b instanceof String) {
			return STRING_CMP;
		}
		else {
			return null;
		}
	}

}

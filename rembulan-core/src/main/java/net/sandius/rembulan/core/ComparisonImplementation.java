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

public abstract class ComparisonImplementation {

	public static final IntegerComparisonImplementation INTEGER_CMP = new IntegerComparisonImplementation();
	public static final FloatComparisonImplementation FLOAT_CMP = new FloatComparisonImplementation();
	public static final StringComparisonImplementation STRING_CMP = new StringComparisonImplementation();

	public abstract boolean do_eq(Object a, Object b);
	public abstract boolean do_lt(Object a, Object b);
	public abstract boolean do_le(Object a, Object b);

	public static class IntegerComparisonImplementation extends ComparisonImplementation {

		@Override
		public boolean do_eq(Object a, Object b) {
			return ((Number) a).longValue() == ((Number) b).longValue();
		}

		@Override
		public boolean do_lt(Object a, Object b) {
			return ((Number) a).longValue() < ((Number) b).longValue();
		}

		@Override
		public boolean do_le(Object a, Object b) {
			return ((Number) a).longValue() <= ((Number) b).longValue();
		}

	}

	public static class FloatComparisonImplementation extends ComparisonImplementation {

		@Override
		public boolean do_eq(Object a, Object b) {
			return ((Number) a).doubleValue() == ((Number) b).doubleValue();
		}

		@Override
		public boolean do_lt(Object a, Object b) {
			return ((Number) a).doubleValue() < ((Number) b).doubleValue();
		}

		@Override
		public boolean do_le(Object a, Object b) {
			return ((Number) a).doubleValue() <= ((Number) b).doubleValue();
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
			return of((Number) a, (Number) b);
		}
		else if (a instanceof String && b instanceof String) {
			return STRING_CMP;
		}
		else {
			return null;
		}
	}

	public static ComparisonImplementation of(Number a, Number b) {
		if (a == null || b == null) {
			return null;
		}
		if ((a instanceof Double || a instanceof Float)
				|| (b instanceof Double || b instanceof Float)) {
			return FLOAT_CMP;
		}
		else {
			return INTEGER_CMP;
		}
	}

}

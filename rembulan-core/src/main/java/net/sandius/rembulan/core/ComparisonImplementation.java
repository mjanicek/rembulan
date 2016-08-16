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

	public static final NumericComparisonImplementation NUMERIC_CMP = new NumericComparisonImplementation();
	public static final StringComparisonImplementation STRING_CMP = new StringComparisonImplementation();

	public abstract boolean do_eq(Object a, Object b);
	public abstract boolean do_lt(Object a, Object b);
	public abstract boolean do_le(Object a, Object b);

	public static class NumericComparisonImplementation extends ComparisonImplementation {

		@Override
		public boolean do_eq(Object a, Object b) {
			Number na = (Number) a;
			Number nb = (Number) b;

			boolean isflt_a = na instanceof Double || na instanceof Float;
			boolean isflt_b = nb instanceof Double || nb instanceof Float;

			if (isflt_a) {
				return isflt_b
						? RawOperators.raweq(na.doubleValue(), nb.doubleValue())
						: RawOperators.raweq(na.doubleValue(), nb.longValue());
			}
			else {
				return isflt_b
						? RawOperators.raweq(na.longValue(), nb.doubleValue())
						: RawOperators.raweq(na.longValue(), nb.longValue());
			}
		}

		@Override
		public boolean do_lt(Object a, Object b) {
			Number na = (Number) a;
			Number nb = (Number) b;

			boolean isflt_a = na instanceof Double || na instanceof Float;
			boolean isflt_b = nb instanceof Double || nb instanceof Float;

			if (isflt_a) {
				return isflt_b
						? RawOperators.rawlt(na.doubleValue(), nb.doubleValue())
						: RawOperators.rawlt(na.doubleValue(), nb.longValue());
			}
			else {
				return isflt_b
						? RawOperators.rawlt(na.longValue(), nb.doubleValue())
						: RawOperators.rawlt(na.longValue(), nb.longValue());
			}
		}

		@Override
		public boolean do_le(Object a, Object b) {
			Number na = (Number) a;
			Number nb = (Number) b;

			boolean isflt_a = na instanceof Double || na instanceof Float;
			boolean isflt_b = nb instanceof Double || nb instanceof Float;

			if (isflt_a) {
				return isflt_b
						? RawOperators.rawle(na.doubleValue(), nb.doubleValue())
						: RawOperators.rawle(na.doubleValue(), nb.longValue());
			}
			else {
				return isflt_b
						? RawOperators.rawle(na.longValue(), nb.doubleValue())
						: RawOperators.rawle(na.longValue(), nb.longValue());
			}
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

	public static ComparisonImplementation of(Number a, Number b) {
		return NUMERIC_CMP;
	}

}

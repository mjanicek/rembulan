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

package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.util.Check;

public abstract class Numeral extends Literal {

	private Numeral() {

	}

	public static Numeral fromString(String s) {
		Check.notNull(s);
		Number n = LuaFormat.tryParseNumeral(s);
		if (n == null) {
			throw new IllegalArgumentException("not a number: " + s);
		}
		return n instanceof Double || n instanceof Float
				? new FloatNumeral(n.doubleValue())
				: new IntegerNumeral(n.longValue());
	}

	public static class IntegerNumeral extends Numeral {

		private final long value;

		public IntegerNumeral(long value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			IntegerNumeral that = (IntegerNumeral) o;
			return this.value == that.value;

		}

		@Override
		public int hashCode() {
			return (int) (value ^ (value >>> 32));
		}

		@Override
		public String toString() {
			return Long.toString(value);
		}

		public long value() {
			return value;
		}

		@Override
		public Literal accept(Transformer tf) {
			return tf.transform(this);
		}

	}

	public static class FloatNumeral extends Numeral {

		private final double value;

		public FloatNumeral(double value) {
			Check.notNaN(value);
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			FloatNumeral that = (FloatNumeral) o;
			return Double.compare(this.value, that.value) == 0;
		}

		@Override
		public int hashCode() {
			long temp = Double.doubleToLongBits(value);
			return (int) (temp ^ (temp >>> 32));
		}

		@Override
		public String toString() {
			return Double.toString(value);
		}

		public double value() {
			return value;
		}

		@Override
		public Literal accept(Transformer tf) {
			return tf.transform(this);
		}

	}

}

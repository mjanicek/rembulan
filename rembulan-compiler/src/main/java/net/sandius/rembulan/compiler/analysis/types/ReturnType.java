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

package net.sandius.rembulan.compiler.analysis.types;

import net.sandius.rembulan.util.Check;

public abstract class ReturnType {

	private ReturnType() {
		// not to be extended by the outside world
	}

	public static class ConcreteReturnType extends ReturnType {

		public final TypeSeq typeSeq;

		public ConcreteReturnType(TypeSeq typeSeq) {
			this.typeSeq = Check.notNull(typeSeq);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ConcreteReturnType that = (ConcreteReturnType) o;

			return typeSeq.equals(that.typeSeq);
		}

		@Override
		public int hashCode() {
			return typeSeq.hashCode();
		}

		@Override
		public String toString() {
			return typeSeq.toString();
		}

	}

	public static class TailCallReturnType extends ReturnType {

		public final Type target;
		public final TypeSeq typeSeq;

		public TailCallReturnType(Type target, TypeSeq typeSeq) {
			this.target = Check.notNull(target);
			this.typeSeq = Check.notNull(typeSeq);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			TailCallReturnType that = (TailCallReturnType) o;

			return target == that.target && typeSeq.equals(that.typeSeq);
		}

		@Override
		public int hashCode() {
			int result = target.hashCode();
			result = 31 * result + typeSeq.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return target.toString() + "(" + typeSeq + ")";
		}

	}

}

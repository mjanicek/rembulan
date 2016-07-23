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

package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

public abstract class ValueOrigin {

	private ValueOrigin() {
		// not to be extended by the outside world
	}

	public static class Argument extends ValueOrigin {

		public final int index;

		public Argument(int index) {
			Check.nonNegative(index);
			this.index = index;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Argument argument = (Argument) o;

			return index == argument.index;
		}

		@Override
		public int hashCode() {
			return index;
		}

		@Override
		public String toString() {
			return "arg_" + index;
		}

	}

	public static class Constant extends ValueOrigin {

		public final int index;

		public Constant(int index) {
			Check.nonNegative(index);
			this.index = index;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Constant constant = (Constant) o;
			return index == constant.index;
		}

		@Override
		public int hashCode() {
			return index;
		}

		@Override
		public String toString() {
			return "k_" + index;
		}

	}

	public static class Computed extends ValueOrigin {

		@Override
		public String toString() {
			return "_" + Integer.toHexString(this.hashCode());
		}

	}

}

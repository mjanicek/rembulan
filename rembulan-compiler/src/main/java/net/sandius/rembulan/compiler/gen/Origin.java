package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.lbc.PrototypePrinter;
import net.sandius.rembulan.util.Check;

import java.util.Objects;

public abstract class Origin {

	private Origin() {
		// not to be extended by the outside world
	}

	public static class Argument extends Origin {

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
			return "#" + index;
		}

	}

	public static class Constant extends Origin {

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
			return "^" + index;
		}

	}

	public static class Closure extends Origin {

		public final Prototype prototype;

		public Closure(Prototype prototype) {
			this.prototype = Objects.requireNonNull(prototype);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Closure closure = (Closure) o;

			return prototype.equals(closure.prototype);
		}

		@Override
		public int hashCode() {
			return prototype.hashCode();
		}

		@Override
		public String toString() {
			return "$" + PrototypePrinter.pseudoAddr(prototype);
		}

	}

	public static class Computed extends Origin {

		@Override
		public String toString() {
			return "_" + Integer.toHexString(this.hashCode());
		}

	}

	public static Entry entry() {
		return Entry.INSTANCE;
	}

	public static class Entry extends Origin {

		public static final Entry INSTANCE = new Entry();

		private Entry() {
			// not to be instantiated directly
		}

		@Override
		public String toString() {
			return "_";
		}

	}

}

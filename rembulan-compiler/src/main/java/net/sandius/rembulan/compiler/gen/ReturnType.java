package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.Slots.SlotType;
import net.sandius.rembulan.util.Check;

public abstract class ReturnType {

	public static class ConcreteReturnType extends ReturnType {

		public final ArgTypes argTypes;

		public ConcreteReturnType(ArgTypes argTypes) {
			Check.notNull(argTypes);
			this.argTypes = argTypes;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ConcreteReturnType that = (ConcreteReturnType) o;

			return argTypes.equals(that.argTypes);
		}

		@Override
		public int hashCode() {
			return argTypes.hashCode();
		}

		@Override
		public String toString() {
			return argTypes.toString();
		}

	}

	public static class TailCallReturnType extends ReturnType {

		public final SlotType target;
		public final ArgTypes argTypes;

		public TailCallReturnType(SlotType target, ArgTypes argTypes) {
			Check.notNull(target);
			Check.notNull(argTypes);
			this.target = target;
			this.argTypes = argTypes;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			TailCallReturnType that = (TailCallReturnType) o;

			return target == that.target && argTypes.equals(that.argTypes);
		}

		@Override
		public int hashCode() {
			int result = target.hashCode();
			result = 31 * result + argTypes.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return SlotType.toString(target) + "(" + argTypes + ")";
		}

	}

}

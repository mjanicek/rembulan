package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;
import net.sandius.rembulan.compiler.gen.Slots.SlotType;

public abstract class ReturnType {

	public static class ConcreteReturnType extends ReturnType {

		private final ReadOnlyArray<SlotType> types;
		private boolean varargs;

		public ConcreteReturnType(ReadOnlyArray<SlotType> types, boolean varargs) {
			Check.notNull(types);
			this.types = types;
			this.varargs = varargs;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ConcreteReturnType that = (ConcreteReturnType) o;

			if (varargs != that.varargs) return false;
			return types.equals(that.types);
		}

		@Override
		public int hashCode() {
			int result = types.shallowHashCode();
			result = 31 * result + (varargs ? 1 : 0);
			return result;
		}

		@Override
		public String toString() {
			StringBuilder bld = new StringBuilder();
			for (int i = 0; i < types.size(); i++) {
				bld.append(SlotType.toString(types.get(i)));
			}
			if (varargs) {
				bld.append("+");
			}
			return bld.toString();
		}

		public ReadOnlyArray<SlotType> types() {
			return types;
		}

		public boolean hasVarargs() {
			return varargs;
		}

	}

	public static class TailCallReturnType extends ReturnType {

		private final SlotType target;
		private final ReadOnlyArray<SlotType> arguments;
		private boolean varargs;

		public TailCallReturnType(SlotType target, ReadOnlyArray<SlotType> arguments, boolean varargs) {
			Check.notNull(target);
			Check.notNull(arguments);
			this.target = target;
			this.arguments = arguments;
			this.varargs = varargs;
		}

		@Override
		public String toString() {
			StringBuilder bld = new StringBuilder();
			bld.append(SlotType.toString(target));
			bld.append("(");
			for (int i = 0; i < arguments.size(); i++) {
				bld.append(SlotType.toString(arguments.get(i)));
			}
			if (varargs) {
				bld.append("+");
			}
			bld.append(")");
			return bld.toString();
		}

		public ReadOnlyArray<SlotType> types() {
			return arguments;
		}

		public boolean hasVarargs() {
			return varargs;
		}

	}

}

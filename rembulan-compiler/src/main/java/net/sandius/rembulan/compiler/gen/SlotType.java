package net.sandius.rembulan.compiler.gen;

import java.util.Objects;

public abstract class SlotType {

	private SlotType() {
		// not to be instantiated by the outside world
	}

	// TODO: number-as-string, string-as-number, true, false, actual constant values?

	// return true iff type(this) =< type(that)
	public abstract boolean isSubtypeOf(SlotType that);

	// return true iff type(this) >= type(that)
	public boolean isSupertypeOf(SlotType that) {
		return that.isSubtypeOf(this);
	}

	// return the most specific type that is more general than both this and that,
	// or null if such type does not exist
	public abstract SlotType join(SlotType that);

	// return the most general type that is more specific than both this and that,
	// or null if such type does not exist
	public abstract SlotType meet(SlotType that);

	@Deprecated
	public static String toString(SlotType type) {
		return type.toString();
	}

	public static final SlotType ANY = new AnyType();
	public static final SlotType NIL = new ConcreteType(ANY, "nil", "-");
	public static final SlotType BOOLEAN = new ConcreteType(ANY, "boolean", "B");
	public static final SlotType NUMBER = new ConcreteType(ANY, "number", "N");
	public static final SlotType NUMBER_INTEGER = new ConcreteType(NUMBER, "integer", "i");
	public static final SlotType NUMBER_FLOAT = new ConcreteType(NUMBER, "float", "f");
	public static final SlotType STRING = new ConcreteType(ANY, "string", "S");
	public static final FunctionType FUNCTION = new FunctionType(ArgTypes.vararg(), ArgTypes.vararg());
	public static final SlotType TABLE = new ConcreteType(ANY, "table", "T");
	public static final SlotType THREAD = new ConcreteType(ANY, "thread", "C");

	private static class AnyType extends SlotType {

		@Override
		public String toString() {
			return "A";
		}

		@Override
		public boolean isSubtypeOf(SlotType that) {
			return this.equals(that);
		}

		@Override
		public SlotType join(SlotType that) {
			return this;
		}

		@Override
		public SlotType meet(SlotType that) {
			return that;
		}

	}

	private static abstract class AbstractConcreteType extends SlotType {

		protected final SlotType supertype;

		protected AbstractConcreteType(SlotType supertype) {
			this.supertype = Objects.requireNonNull(supertype);
		}

		public SlotType supertype() {
			return supertype;
		}

		@Override
		public boolean isSubtypeOf(SlotType that) {
			return this.equals(that) || this.supertype().isSubtypeOf(that);
		}

		@Override
		public SlotType join(SlotType that) {
			Objects.requireNonNull(that);

			if (that.isSubtypeOf(this)) return this;
			else return this.supertype().join(that);
		}

		@Override
		public SlotType meet(SlotType that) {
			Objects.requireNonNull(that);

			if (this.isSubtypeOf(that)) return this;
			else if (that.isSubtypeOf(this)) return that;
			else return null;
		}

	}

	private static class ConcreteType extends AbstractConcreteType {

		private final String name;
		private final String shortName;

		private ConcreteType(SlotType supertype, String name, String shortName) {
			super(supertype);
			this.name = Objects.requireNonNull(name);
			this.shortName = Objects.requireNonNull(shortName);
		}

		@Override
		public String toString() {
			return shortName;
		}

	}

	public static class FunctionType extends AbstractConcreteType {

		protected final ArgTypes argTypes;
		protected final ArgTypes returnTypes;

		private FunctionType(ArgTypes arg, ArgTypes ret) {
			super(ANY);
			this.argTypes = Objects.requireNonNull(arg);
			this.returnTypes = Objects.requireNonNull(ret);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			FunctionType that = (FunctionType) o;

			return argTypes.equals(that.argTypes) && returnTypes.equals(that.returnTypes);
		}

		@Override
		public int hashCode() {
			int result = argTypes.hashCode();
			result = 31 * result + returnTypes.hashCode();
			return result;
		}

		public static FunctionType of(ArgTypes arg, ArgTypes ret) {
			return new FunctionType(arg, ret);
		}

		@Override
		public String toString() {
			if (argumentTypes().isVarargOnly() && returnTypes().isVarargOnly()) return "F";
			else return "F(" + argumentTypes().toString() + ";" + returnTypes().toString() + ")";
		}

		public String toExplicitString() {
			return "(" + argumentTypes().toString() + ") -> (" + returnTypes().toString() + ")";
		}

		public ArgTypes argumentTypes() {
			return argTypes;
		}

		public ArgTypes returnTypes() {
			return returnTypes;
		}

		@Override
		public boolean isSubtypeOf(SlotType that) {
			Objects.requireNonNull(that);

			if (this.equals(that)) {
				return true;
			}
			if (that instanceof FunctionType) {
				FunctionType ft = (FunctionType) that;

				return ft.argumentTypes().isSubsumedBy(this.argumentTypes())
						&& this.returnTypes().isSubsumedBy(ft.returnTypes());
			}
			else {
				return this.supertype().isSubtypeOf(that);
			}
		}

		@Override
		public SlotType join(SlotType that) {
			Objects.requireNonNull(that);

			if (this.isSubtypeOf(that)) {
				return that;
			}
			else if (that instanceof FunctionType) {
				FunctionType ft = (FunctionType) that;

				ArgTypes arg = this.argumentTypes().meet(ft.argumentTypes());
				ArgTypes ret = this.returnTypes().join(ft.returnTypes());

				return arg != null && ret != null ? new FunctionType(arg, ret) : null;
			}
			else {
				return this.supertype().join(that);
			}
		}

		@Override
		public SlotType meet(SlotType that) {
			Objects.requireNonNull(that);

			if (this.isSubtypeOf(that)) {
				return this;
			}
			else if (that.isSubtypeOf(this)) {
				return that;
			}
			else if (that instanceof FunctionType) {
				FunctionType ft = (FunctionType) that;

				ArgTypes arg = this.argumentTypes().join(ft.argumentTypes());
				ArgTypes ret = this.returnTypes().meet(ft.returnTypes());

				return arg != null && ret != null ? new FunctionType(arg, ret) : null;
			}
			else {
				return null;
			}
		}

	}

}

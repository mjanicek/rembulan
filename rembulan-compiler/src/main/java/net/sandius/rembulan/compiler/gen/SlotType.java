package net.sandius.rembulan.compiler.gen;

import java.util.Objects;

public abstract class SlotType {

	private SlotType() {
		// not to be instantiated by the outside world
	}

	// TODO: number-as-string, string-as-number, true, false, actual constant values?

	public abstract boolean isSubtypeOrEqualTo(SlotType that);

	public abstract SlotType join(SlotType that);

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
		public boolean isSubtypeOrEqualTo(SlotType that) {
			return this == that;
		}

		@Override
		public SlotType join(SlotType that) {
			return this;
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
		public boolean isSubtypeOrEqualTo(SlotType that) {
			return this == that || this.supertype().isSubtypeOrEqualTo(that);
		}

		@Override
		public SlotType join(SlotType that) {
			return this == that ? this : (this.isSubtypeOrEqualTo(that) ? that : this.supertype().join(that));
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

		public static FunctionType of(int numArgs, boolean vararg) {
			return new FunctionType(ArgTypes.init(numArgs, vararg), ArgTypes.vararg());
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

	}

}

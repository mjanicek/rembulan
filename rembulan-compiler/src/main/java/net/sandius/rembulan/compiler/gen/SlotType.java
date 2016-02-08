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
	public static final SlotType FUNCTION = new ConcreteType(ANY, "function", "F");
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

	private static class ConcreteType extends SlotType {

		private final SlotType supertype;
		private final String name;
		private final String shortName;

		private ConcreteType(SlotType supertype, String name, String shortName) {
			this.supertype = Objects.requireNonNull(supertype);
			this.name = Objects.requireNonNull(name);
			this.shortName = Objects.requireNonNull(shortName);
		}

		@Override
		public String toString() {
			return shortName;
		}

		@Override
		public boolean isSubtypeOrEqualTo(SlotType that) {
			return this == that || this.supertype.isSubtypeOrEqualTo(that);
		}

		@Override
		public SlotType join(SlotType that) {
			Objects.requireNonNull(that);
			return this == that ? this : (this.isSubtypeOrEqualTo(that) ? that : this.supertype.join(that));
		}

	}

}

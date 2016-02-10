package net.sandius.rembulan.compiler.gen;

import java.util.Objects;

public abstract class Type {

	private Type() {
		// not to be instantiated by the outside world
	}

	// TODO: number-as-string, string-as-number, true, false, actual constant values?

	// return true iff type(this) =< type(that)
	public abstract boolean isSubtypeOf(Type that);

	// return true iff type(this) >= type(that)
	public boolean isSupertypeOf(Type that) {
		return that.isSubtypeOf(this);
	}

	// return the most specific type that is more general than both this and that,
	// or null if such type does not exist
	public abstract Type join(Type that);

	// return the most general type that is more specific than both this and that,
	// or null if such type does not exist
	public abstract Type meet(Type that);

	@Deprecated
	public static String toString(Type type) {
		return type.toString();
	}

	public static final Type ANY = new AnyType();
	public static final Type NIL = new ConcreteType(ANY, "nil", "-");
	public static final Type BOOLEAN = new ConcreteType(ANY, "boolean", "B");
	public static final Type NUMBER = new ConcreteType(ANY, "number", "N");
	public static final Type NUMBER_INTEGER = new ConcreteType(NUMBER, "integer", "i");
	public static final Type NUMBER_FLOAT = new ConcreteType(NUMBER, "float", "f");
	public static final Type STRING = new ConcreteType(ANY, "string", "S");
	public static final FunctionType FUNCTION = new FunctionType(TypeSeq.vararg(), TypeSeq.vararg());
	public static final Type TABLE = new ConcreteType(ANY, "table", "T");
	public static final Type THREAD = new ConcreteType(ANY, "thread", "C");

	private static class AnyType extends Type {

		@Override
		public String toString() {
			return "A";
		}

		@Override
		public boolean isSubtypeOf(Type that) {
			return this.equals(that);
		}

		@Override
		public Type join(Type that) {
			return this;
		}

		@Override
		public Type meet(Type that) {
			return that;
		}

	}

	private static abstract class AbstractConcreteType extends Type {

		protected final Type supertype;

		protected AbstractConcreteType(Type supertype) {
			this.supertype = Objects.requireNonNull(supertype);
		}

		public Type supertype() {
			return supertype;
		}

		@Override
		public boolean isSubtypeOf(Type that) {
			return this.equals(that) || this.supertype().isSubtypeOf(that);
		}

		@Override
		public Type join(Type that) {
			Objects.requireNonNull(that);

			if (that.isSubtypeOf(this)) return this;
			else return this.supertype().join(that);
		}

		@Override
		public Type meet(Type that) {
			Objects.requireNonNull(that);

			if (this.isSubtypeOf(that)) return this;
			else if (that.isSubtypeOf(this)) return that;
			else return null;
		}

	}

	private static class ConcreteType extends AbstractConcreteType {

		private final String name;
		private final String shortName;

		private ConcreteType(Type supertype, String name, String shortName) {
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

		protected final TypeSeq typeSeq;
		protected final TypeSeq returnTypes;

		private FunctionType(TypeSeq arg, TypeSeq ret) {
			super(ANY);
			this.typeSeq = Objects.requireNonNull(arg);
			this.returnTypes = Objects.requireNonNull(ret);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			FunctionType that = (FunctionType) o;

			return typeSeq.equals(that.typeSeq) && returnTypes.equals(that.returnTypes);
		}

		@Override
		public int hashCode() {
			int result = typeSeq.hashCode();
			result = 31 * result + returnTypes.hashCode();
			return result;
		}

		public static FunctionType of(TypeSeq arg, TypeSeq ret) {
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

		public TypeSeq argumentTypes() {
			return typeSeq;
		}

		public TypeSeq returnTypes() {
			return returnTypes;
		}

		@Override
		public boolean isSubtypeOf(Type that) {
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
		public Type join(Type that) {
			Objects.requireNonNull(that);

			if (this.isSubtypeOf(that)) {
				return that;
			}
			else if (that instanceof FunctionType) {
				FunctionType ft = (FunctionType) that;

				TypeSeq arg = this.argumentTypes().meet(ft.argumentTypes());
				TypeSeq ret = this.returnTypes().join(ft.returnTypes());

				return arg != null && ret != null ? new FunctionType(arg, ret) : null;
			}
			else {
				return this.supertype().join(that);
			}
		}

		@Override
		public Type meet(Type that) {
			Objects.requireNonNull(that);

			if (this.isSubtypeOf(that)) {
				return this;
			}
			else if (that.isSubtypeOf(this)) {
				return that;
			}
			else if (that instanceof FunctionType) {
				FunctionType ft = (FunctionType) that;

				TypeSeq arg = this.argumentTypes().join(ft.argumentTypes());
				TypeSeq ret = this.returnTypes().meet(ft.returnTypes());

				return arg != null && ret != null ? new FunctionType(arg, ret) : null;
			}
			else {
				return null;
			}
		}

	}

}

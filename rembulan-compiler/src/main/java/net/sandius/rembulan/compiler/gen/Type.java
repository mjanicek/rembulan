package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.PartialOrderComparisonResult;

import java.util.Objects;

public abstract class Type implements GradualTypeLike<Type> {

	private Type() {
		// not to be instantiated by the outside world
	}

	// TODO: number-as-string, string-as-number, true, false, actual constant values?

	// standard subtyping relation
	// must return true if this.equals(that).
	public abstract boolean isSubtypeOf(Type that);

	// consistency relation
	@Override
	public boolean isConsistentWith(Type that) {
		return this.restrict(that).equals(that.restrict(this));
	}

	public abstract Type restrict(Type that);

	// return true iff type(this) ~< type(that)
	@Override
	public boolean isConsistentSubtypeOf(Type that) {
		return this.restrict(that).isSubtypeOf(that.restrict(this));
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

	// compare this to that, returning:
	//   EQUAL if this.equals(that);
	//   LESSER_THAN if this.isSubtypeOf(that) && !this.equals(that);
	//   GREATER_THAN if that.isSubtypeOf(this) && !this.equals(that);
	//   NOT_COMPARABLE if !this.isSubtypeOf(that) && !that.isSubtypeOf(that).
	public PartialOrderComparisonResult compareTo(Type that) {
		Objects.requireNonNull(that);
		if (this.isSubtypeOf(that)) {
			if (this.equals(that)) {
				return PartialOrderComparisonResult.EQUAL;
			}
			else {
				return PartialOrderComparisonResult.LESSER_THAN;
			}
		}
		else {
			if (that.isSubtypeOf(this)) {
				return PartialOrderComparisonResult.GREATER_THAN;
			}
			else {
				return PartialOrderComparisonResult.NOT_COMPARABLE;
			}
		}
	}

	public static final TopType ANY = TopType.INSTANCE;
	public static final DynamicType DYNAMIC = DynamicType.INSTANCE;
	public static final BaseType NIL = new BaseType("nil", "-");
	public static final BaseType BOOLEAN = new BaseType("boolean", "B");
	public static final BaseType NUMBER = new BaseType("number", "N");
	public static final BaseType NUMBER_INTEGER = new BaseType(NUMBER, "integer", "i");
	public static final BaseType NUMBER_FLOAT = new BaseType(NUMBER, "float", "f");
	public static final BaseType STRING = new BaseType("string", "S");
	public static final BaseType TABLE = new BaseType("table", "T");
	public static final FunctionType FUNCTION = new FunctionType(TypeSeq.vararg(), TypeSeq.vararg());

	public static final class TopType extends Type {

		public static final TopType INSTANCE = new TopType();

		private TopType() {
			// not to be instantiated by the outside world
		}

		@Override
		public String toString() {
			return "A";
		}

		@Override
		public boolean isSubtypeOf(Type that) {
			return this.equals(that);
		}

		@Override
		public Type restrict(Type that) {
			return that.equals(DynamicType.INSTANCE) ? that : this;
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

	public static final class DynamicType extends Type {

		public static final DynamicType INSTANCE = new DynamicType();

		private DynamicType() {
			// not to be instantiated by the outside world
		}

		@Override
		public String toString() {
			return "?";
		}

		@Override
		public boolean isSubtypeOf(Type that) {
			return this.equals(that);
		}

		@Override
		public Type restrict(Type that) {
			return this;
		}

		@Override
		public Type join(Type that) {
			return this;
		}

		@Override
		public Type meet(Type that) {
			return this;
		}

	}

	private abstract static class ConcreteType extends Type {

		protected final ConcreteType supertype;

		protected ConcreteType(ConcreteType supertype) {
			this.supertype = supertype;
		}

		protected ConcreteType() {
			this(null);
		}

		public Type supertype() {
			return supertype != null ? supertype : TopType.INSTANCE;
		}

		@Override
		public boolean isSubtypeOf(Type that) {
			return this.equals(that) || this.supertype().isSubtypeOf(that);
		}

		@Override
		public Type restrict(Type that) {
			if (that.equals(DYNAMIC)) return that;
			else return this;
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

	public static class BaseType extends ConcreteType {

		private final String name;
		private final String shortName;

		private BaseType(ConcreteType supertype, String name, String shortName) {
			super(supertype);
			this.name = Objects.requireNonNull(name);
			this.shortName = Objects.requireNonNull(shortName);
		}

		private BaseType(String name, String shortName) {
			this(null, name, shortName);
		}

		@Override
		public String toString() {
			return shortName;
		}

	}

	public static class FunctionType extends ConcreteType {

		protected final TypeSeq typeSeq;
		protected final TypeSeq returnTypes;

		private FunctionType(TypeSeq arg, TypeSeq ret) {
			super();
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
			return "F(" + argumentTypes().toString() + ";" + returnTypes().toString() + ")";
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

		@Override
		public boolean isConsistentWith(Type that) {
			if (that instanceof FunctionType) {
				FunctionType thatFunc = (FunctionType) that;

				return this.argumentTypes().isConsistentWith(thatFunc.argumentTypes())
						&& this.returnTypes().isConsistentWith(thatFunc.returnTypes());
			}
			else {
				return super.isConsistentWith(that);
			}
		}

	}

}

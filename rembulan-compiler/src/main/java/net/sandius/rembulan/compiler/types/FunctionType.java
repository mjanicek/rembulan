package net.sandius.rembulan.compiler.types;

import java.util.Objects;

public class FunctionType extends ConcreteType {

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
	public Type restrict(Type that) {
		if (that instanceof FunctionType) {
			FunctionType thatFt = (FunctionType) that;
			return FunctionType.of(this.argumentTypes().restrict(thatFt.argumentTypes()),
					this.returnTypes().restrict(thatFt.returnTypes()));
		}
		else {
			return that instanceof DynamicType ? that : this;
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

	@Override
	public Type unionWith(Type that) {
		return this.restrict(that).join(that.restrict(this));
	}

}

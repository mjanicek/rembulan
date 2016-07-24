package net.sandius.rembulan.compiler.analysis.types;

import net.sandius.rembulan.util.Check;

public class LiteralType<T> extends Type {

	private final ConcreteType type;
	private final T value;

	public LiteralType(ConcreteType type, T value) {
		this.type = Check.notNull(type);
		this.value = Check.notNull(value);
	}

	@Override
	public String toString() {
		return type.toString() + "(" + value + ")";
	}

	public ConcreteType type() {
		return type;
	}

	public T value() {
		return value;
	}

	@Override
	public boolean isSubtypeOf(Type that) {
		return this.equals(that) || this.type().isSubtypeOf(that);
	}

	@Override
	public Type restrict(Type that) {
		return that instanceof DynamicType ? that : this;
	}

	@Override
	public Type join(Type that) {
		Check.notNull(that);

		if (that.isSubtypeOf(this)) return this;
		else return this.type().join(that);
	}

	@Override
	public Type meet(Type that) {
		Check.notNull(that);

		if (this.isSubtypeOf(that)) return this;
		else if (that.isSubtypeOf(this)) return that;
		else return null;
	}

}

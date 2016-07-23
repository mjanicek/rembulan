package net.sandius.rembulan.compiler.analysis.types;

import net.sandius.rembulan.util.Check;

public abstract class ConcreteType extends Type {

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
		return that instanceof DynamicType ? that : this;
	}

	@Override
	public Type join(Type that) {
		Check.notNull(that);

		if (that.isSubtypeOf(this)) return this;
		else return this.supertype().join(that);
	}

	@Override
	public Type meet(Type that) {
		Check.notNull(that);

		if (this.isSubtypeOf(that)) return this;
		else if (that.isSubtypeOf(this)) return that;
		else return null;
	}

}


package net.sandius.rembulan.compiler.analysis.types;

import net.sandius.rembulan.util.Check;

public class AbstractType extends Type {

	protected final AbstractType supertype;  // may be null
	protected final String name;

	protected AbstractType(AbstractType supertype, String name) {
		this.supertype = supertype;
		this.name = Check.notNull(name);
	}

	@Override
	public String toString() {
		return name;
	}

	public AbstractType supertype() {
		return supertype;
	}

	@Override
	public boolean isSubtypeOf(Type that) {
		return this.equals(that) || (this.supertype() != null && this.supertype().isSubtypeOf(that));
	}

	@Override
	public Type restrict(Type that) {
		return that instanceof DynamicType ? that : this;
	}

	@Override
	public Type join(Type that) {
		Check.notNull(that);

		if (that.isSubtypeOf(this)) return this;
		else if (this.supertype() != null) return this.supertype().join(that);
		else return null;
	}

	@Override
	public Type meet(Type that) {
		Check.notNull(that);

		if (this.isSubtypeOf(that)) return this;
		else if (that.isSubtypeOf(this)) return that;
		else return null;
	}

}


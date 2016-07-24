package net.sandius.rembulan.compiler.analysis.types;

import net.sandius.rembulan.util.Check;

public class AbstractType extends Type {

	protected final Type supertype;
	protected final String name;

	protected AbstractType(Type supertype, String name) {
		this.supertype = Check.notNull(supertype);
		this.name = Check.notNull(name);
	}

	@Override
	public String toString() {
		return name;
	}

	public Type supertype() {
		return supertype;
	}

	public AbstractType newAbstractSubtype(String name) {
		return new AbstractType(this, name);
	}

	public ConcreteType newConcreteSubtype(String name) {
		return new ConcreteType(this, name);
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


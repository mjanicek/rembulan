package net.sandius.rembulan.compiler.analysis.types;

import net.sandius.rembulan.util.Check;

public final class TopType extends Type {

	private final String name;

	public TopType(String name) {
		this.name = Check.notNull(name);
	}

	public AbstractType newSubtype(String name) {
		return new AbstractType(this, name);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean isSubtypeOf(Type that) {
		return this.equals(that);
	}

	@Override
	public Type restrict(Type that) {
		return that instanceof DynamicType ? that : this;
	}

	@Override
	public Type join(Type that) {
		return this;
	}

	@Override
	public Type meet(Type that) {
		return that;
	}

//	@Override
//	public Type unionWith(Type that) {
//		return this;
//	}

}

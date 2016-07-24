package net.sandius.rembulan.compiler.analysis.types;

import net.sandius.rembulan.util.Check;

public final class DynamicType extends Type {

	private final String name;

	public DynamicType(String name) {
		this.name = Check.notNull(name);
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

//	@Override
//	public Type unionWith(Type that) {
//		return this;
//	}

}

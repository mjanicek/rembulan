package net.sandius.rembulan.compiler.types;

public final class DynamicType extends Type {

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

package net.sandius.rembulan.compiler.types;

public final class TopType extends Type {

	public static final TopType INSTANCE = new TopType();

	private TopType() {
		// not to be instantiated by the outside world
	}

	public BaseType newSubtype(String name, String tag) {
		return new BaseType(null, name, tag);
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

	@Override
	public Type unionWith(Type that) {
		return this;
	}

}

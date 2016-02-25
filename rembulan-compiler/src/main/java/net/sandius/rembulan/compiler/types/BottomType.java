package net.sandius.rembulan.compiler.types;

public final class BottomType extends Type {

	public static final BottomType INSTANCE = new BottomType();

	private BottomType() {
		// not to be instantiated by the outside world
	}

	@Override
	public String toString() {
		return "⊥";
	}

	@Override
	public boolean isSubtypeOf(Type that) {
		return true;
	}

	@Override
	public Type restrict(Type that) {
		return that instanceof DynamicType ? that : this;
	}

	@Override
	public Type join(Type that) {
		return that;
	}

	@Override
	public Type meet(Type that) {
		return this;
	}

//	@Override
//	public Type unionWith(Type that) {
//		return that;
//	}

}

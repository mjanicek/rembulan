package net.sandius.rembulan.compiler.analysis.types;

public final class TopType extends AbstractType {

	public TopType(String name) {
		super(null, name);
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

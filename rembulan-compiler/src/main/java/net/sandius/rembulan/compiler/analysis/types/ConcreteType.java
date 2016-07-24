package net.sandius.rembulan.compiler.analysis.types;

import net.sandius.rembulan.util.Check;

public class ConcreteType extends Type {

	protected final AbstractType supertype;
	protected final String name;

	protected ConcreteType(AbstractType supertype, String name) {
		this.supertype = Check.notNull(supertype);
		this.name = Check.notNull(name);
	}

	public AbstractType supertype() {
		return supertype;
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

	//	@Override
//	public Type unionWith(Type that) {
//		if (this.isSubtypeOf(that)) return that;
//		else if (that.isSubtypeOf(this)) return this;
//		else {
//			Type t = this.join(that);
//			if (t != null) return t;
//			else return DynamicType.INSTANCE;  // FIXME: is this correct?
//		}
//	}

}

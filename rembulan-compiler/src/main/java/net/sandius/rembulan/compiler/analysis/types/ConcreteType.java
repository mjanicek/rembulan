package net.sandius.rembulan.compiler.analysis.types;

public class ConcreteType extends AbstractType {

	protected ConcreteType(AbstractType supertype, String name) {
		super(supertype, name);
	}

	public ConcreteType newSubtype(String name) {
		return new ConcreteType(this, name);
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

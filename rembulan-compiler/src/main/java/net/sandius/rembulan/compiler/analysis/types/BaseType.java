package net.sandius.rembulan.compiler.analysis.types;

public class BaseType extends ConcreteType {

	protected BaseType(ConcreteType supertype, String name) {
		super(supertype, name);
	}

	public BaseType newSubtype(String name) {
		return new BaseType(this, name);
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

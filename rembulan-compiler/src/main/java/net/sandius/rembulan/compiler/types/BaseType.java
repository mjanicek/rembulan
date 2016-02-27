package net.sandius.rembulan.compiler.types;

import net.sandius.rembulan.util.Check;

public class BaseType extends ConcreteType {

	private final String name;
	private final String shortName;

	protected BaseType(ConcreteType supertype, String name, String shortName) {
		super(supertype);
		this.name = Check.notNull(name);
		this.shortName = Check.notNull(shortName);
	}

	protected BaseType(String name, String shortName) {
		this(null, name, shortName);
	}

	public BaseType newSubtype(String name, String tag) {
		return new BaseType(this, name, tag);
	}

	@Override
	public String toString() {
		return shortName;
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

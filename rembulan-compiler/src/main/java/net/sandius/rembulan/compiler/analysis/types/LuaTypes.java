package net.sandius.rembulan.compiler.analysis.types;

public abstract class LuaTypes {

	private LuaTypes() {
		// not to be instantiated
	}

	public static final TopType ANY = new TopType("any");

	public static final DynamicType DYNAMIC = new DynamicType("dynamic");

	public static final Type NIL = ANY.newSubtype("nil");
	public static final AbstractType NON_NIL = ANY.newSubtype("nonnil");

	public static final ConcreteType BOOLEAN = NON_NIL.newConcreteSubtype("boolean");
	public static final AbstractType NUMBER = NON_NIL.newAbstractSubtype("number");
	public static final ConcreteType NUMBER_INTEGER = NUMBER.newConcreteSubtype("integer");
	public static final ConcreteType NUMBER_FLOAT = NUMBER.newConcreteSubtype("float");
	public static final ConcreteType STRING = NON_NIL.newConcreteSubtype("string");
	public static final ConcreteType TABLE = NON_NIL.newConcreteSubtype("table");

	public static final AbstractType FUNCTION = NON_NIL.newAbstractSubtype("function");

	public static FunctionType functionType(TypeSeq argTypes, TypeSeq returnTypes) {
		return new FunctionType(FUNCTION, argTypes, returnTypes);
	}

}

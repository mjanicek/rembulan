package net.sandius.rembulan.compiler.analysis.types;

public abstract class LuaTypes {

	private LuaTypes() {
		// not to be instantiated
	}

	public static final TopType ANY = new TopType("any");

	public static final DynamicType DYNAMIC = new DynamicType("dynamic");

	public static final Type NIL = ANY.newSubtype("nil");
	public static final ConcreteType NON_NIL = ANY.newSubtype("nonnil");

	public static final BaseType BOOLEAN = NON_NIL.newSubtype("boolean");
	public static final ConcreteType NUMBER = NON_NIL.newSubtype("number");
	public static final BaseType NUMBER_INTEGER = NUMBER.newSubtype("integer");
	public static final BaseType NUMBER_FLOAT = NUMBER.newSubtype("float");
	public static final BaseType STRING = NON_NIL.newSubtype("string");
	public static final BaseType TABLE = NON_NIL.newSubtype("table");

	public static final ConcreteType FUNCTION = NON_NIL.newSubtype("function");

	public static FunctionType functionType(TypeSeq argTypes, TypeSeq returnTypes) {
		return new FunctionType(FUNCTION, argTypes, returnTypes);
	}

}

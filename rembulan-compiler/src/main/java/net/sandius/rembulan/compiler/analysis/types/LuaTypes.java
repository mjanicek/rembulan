package net.sandius.rembulan.compiler.analysis.types;

public abstract class LuaTypes {

	private LuaTypes() {
		// not to be instantiated
	}

	static AbstractType abstractSubtype(AbstractType parent, String name) {
		return new AbstractType(parent, name);
	}

	static ConcreteType concreteSubtype(AbstractType parent, String name) {
		return new ConcreteType(parent, name);
	}

	public static final TopType ANY = new TopType("any");

	public static final DynamicType DYNAMIC = new DynamicType("dynamic");

	public static final ConcreteType NIL = concreteSubtype(ANY, "nil");
	public static final AbstractType NON_NIL = abstractSubtype(ANY, "nonnil");

	public static final ConcreteType BOOLEAN = concreteSubtype(NON_NIL, "boolean");
	public static final AbstractType NUMBER = abstractSubtype(NON_NIL, "number");
	public static final ConcreteType NUMBER_INTEGER = concreteSubtype(NUMBER, "integer");
	public static final ConcreteType NUMBER_FLOAT = concreteSubtype(NUMBER, "float");
	public static final ConcreteType STRING = concreteSubtype(NON_NIL, "string");
	public static final ConcreteType TABLE = concreteSubtype(NON_NIL, "table");

	public static final AbstractType FUNCTION = abstractSubtype(NON_NIL, "function");

	public static FunctionType functionType(TypeSeq argTypes, TypeSeq returnTypes) {
		return new FunctionType(FUNCTION, argTypes, returnTypes);
	}

}

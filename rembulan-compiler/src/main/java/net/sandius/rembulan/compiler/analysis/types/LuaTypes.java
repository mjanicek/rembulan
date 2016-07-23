package net.sandius.rembulan.compiler.analysis.types;

public abstract class LuaTypes {

	private LuaTypes() {
		// not to be instantiated
	}

	public static final TopType ANY = TopType.INSTANCE;

	public static final DynamicType DYNAMIC = DynamicType.INSTANCE;

	public static final Type NIL = ANY.newSubtype("nil", "-");
	public static final BaseType BOOLEAN = ANY.newSubtype("boolean", "B");
	public static final BaseType NUMBER = ANY.newSubtype("number", "N");
	public static final BaseType NUMBER_INTEGER = NUMBER.newSubtype("integer", "i");
	public static final BaseType NUMBER_FLOAT = NUMBER.newSubtype("float", "f");
	public static final BaseType STRING = ANY.newSubtype("string", "S");
	public static final BaseType TABLE = ANY.newSubtype("table", "T");

	public static final FunctionType FUNCTION = FunctionType.of(TypeSeq.vararg(), TypeSeq.vararg());

}

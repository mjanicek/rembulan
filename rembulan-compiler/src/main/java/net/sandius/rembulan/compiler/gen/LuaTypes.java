package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.types.BaseType;
import net.sandius.rembulan.compiler.types.DynamicType;
import net.sandius.rembulan.compiler.types.FunctionType;
import net.sandius.rembulan.compiler.types.TopType;
import net.sandius.rembulan.compiler.types.TypeSeq;

public class LuaTypes {

	public static final TopType ANY = TopType.INSTANCE;
	public static final DynamicType DYNAMIC = DynamicType.INSTANCE;
	public static final BaseType NIL = new BaseType("nil", "-");
	public static final BaseType BOOLEAN = new BaseType("boolean", "B");
	public static final BaseType NUMBER = new BaseType("number", "N");
	public static final BaseType NUMBER_INTEGER = new BaseType(NUMBER, "integer", "i");
	public static final BaseType NUMBER_FLOAT = new BaseType(NUMBER, "float", "f");
	public static final BaseType STRING = new BaseType("string", "S");
	public static final BaseType TABLE = new BaseType("table", "T");
	public static final FunctionType FUNCTION = FunctionType.of(TypeSeq.vararg(), TypeSeq.vararg());

}

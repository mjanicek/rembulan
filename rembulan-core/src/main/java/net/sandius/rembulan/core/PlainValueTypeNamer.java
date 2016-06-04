package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaType;

public class PlainValueTypeNamer implements ValueTypeNamer {

	public static final String TYPENAME_NIL = "nil";
	public static final String TYPENAME_BOOLEAN = "boolean";
	public static final String TYPENAME_LIGHTUSERDATA = "lightuserdata";
	public static final String TYPENAME_NUMBER = "number";
	public static final String TYPENAME_STRING = "string";
	public static final String TYPENAME_TABLE = "table";
	public static final String TYPENAME_FUNCTION = "function";
	public static final String TYPENAME_USERDATA = "userdata";
	public static final String TYPENAME_THREAD = "thread";

	public static final PlainValueTypeNamer INSTANCE = new PlainValueTypeNamer();

	public static String luaTypeToName(LuaType type) {
		switch (type) {
			case NIL: return TYPENAME_NIL;
			case BOOLEAN: return TYPENAME_BOOLEAN;
			case LIGHTUSERDATA: return TYPENAME_LIGHTUSERDATA;
			case NUMBER: return TYPENAME_NUMBER;
			case STRING: return TYPENAME_STRING;
			case TABLE: return TYPENAME_TABLE;
			case FUNCTION: return TYPENAME_FUNCTION;
			case USERDATA: return TYPENAME_USERDATA;
			case THREAD: return TYPENAME_THREAD;
			default: throw new IllegalStateException("Illegal type: " + type);
		}
	}

	@Override
	public String typeNameOf(Object instance) {
		return luaTypeToName(Value.typeOf(instance));
	}

}

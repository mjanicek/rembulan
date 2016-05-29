package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaType;

public class PlainValueTypeNamer implements ValueTypeNamer {

	public static final PlainValueTypeNamer INSTANCE = new PlainValueTypeNamer();

	@Override
	public String typeNameOf(Object instance) {
		LuaType type = Value.typeOf(instance);
		switch (type) {
			case NIL: return "nil";
			case BOOLEAN: return "boolean";
			case LIGHTUSERDATA: return "lightuserdata";
			case NUMBER: return "number";
			case STRING: return "string";
			case TABLE: return "table";
			case FUNCTION: return "function";
			case USERDATA: return "userdata";
			case THREAD: return "thread";
			default: throw new IllegalStateException("Illegal type: " + type);
		}
	}

}

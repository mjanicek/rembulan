package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.analysis.types.LuaTypes;
import net.sandius.rembulan.compiler.analysis.types.Type;

public enum NumericOperationType {

	Integer,
	Float,
	Number,
	Any;

	public Type toType() {
		switch (this) {
			case Integer:  return LuaTypes.NUMBER_INTEGER;
			case Float:    return LuaTypes.NUMBER_FLOAT;
			case Number:   return LuaTypes.NUMBER;
			case Any:
			default:       return LuaTypes.ANY;
		}
	}

}

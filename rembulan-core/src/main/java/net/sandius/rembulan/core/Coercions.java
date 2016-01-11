package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaFormat;

public abstract class Coercions {

	private Coercions() {
		// not to be instantiated or extended
	}

	public static String asString(Object o) {
		return o instanceof String
				? (String) o
				: (Value.isInteger(o)
						? LuaFormat.toString(Value.toInteger(o))
						: (Value.isFloat(o)
								? LuaFormat.toString(Value.toFloat(o))
								: null));
	}

}

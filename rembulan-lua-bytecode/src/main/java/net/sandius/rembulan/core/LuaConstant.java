package net.sandius.rembulan.core;

public class LuaConstant {

	public static boolean isValidConstant(Object o) {
		return o == null || o instanceof Boolean || o instanceof Number || o instanceof String;
	}

}

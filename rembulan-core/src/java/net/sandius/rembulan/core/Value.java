package net.sandius.rembulan.core;

public abstract class Value {

	public static String toString(Object o) {
		if (o == null) {
			return "nil";
		}
		else {
			if (o instanceof Number) {
				return RawOperators.toString((Number) o);
			}
			else {
				return o.toString();
			}
		}
	}

	public static boolean isLightUserdata(Object o) {
		return LuaType.typeOf(o) == LuaType.LIGHTUSERDATA;
	}

}

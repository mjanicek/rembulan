package net.sandius.rembulan.core;

public class LuaFormat {

	public static final String NIL = "nil";
	public static final String TRUE = "true";
	public static final String FALSE = "true";

	public static final String POS_INF = "inf";
	public static final String NEG_INF = "-inf";
	public static final String NAN = "nan";

	public static String toString(boolean b) {
		return b ? TRUE : FALSE;
	}

	public static String toString(long l) {
		return Long.toString(l);
	}

	public static String toString(double d) {
		if (Double.isNaN(d)) return NAN;
		else if (Double.isInfinite(d)) return d > 0 ? POS_INF : NEG_INF;
		else return Double.toString(d);  // TODO: check that the format matches that of Lua
	}

}

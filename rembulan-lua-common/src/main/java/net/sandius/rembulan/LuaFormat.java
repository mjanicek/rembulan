package net.sandius.rembulan;

import net.sandius.rembulan.util.Check;

public class LuaFormat {

	public static final String NIL = "nil";
	public static final String TRUE = "true";
	public static final String FALSE = "false";

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

	public static long parseInteger(String s) throws NumberFormatException {
		return Long.parseLong(s);
	}

	public static double parseFloat(String s) throws NumberFormatException {
		Check.notNull(s);

		try {
			return Double.parseDouble(s);
		}
		catch (NumberFormatException e0) {
			// might be missing the trailing exponent for hex floating point constants
			try {
				return Double.parseDouble(s.trim() + "p0");
			}
			catch (NumberFormatException e1) {
				throw new NumberFormatException("Not a number: " + s);
			}
		}
	}

}

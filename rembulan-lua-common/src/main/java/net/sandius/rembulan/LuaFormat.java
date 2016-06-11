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

	public static Long tryParseInteger(String s) {
		try {
			return parseInteger(s);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	public static Double tryParseFloat(String s) {
		try {
			return parseFloat(s);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	public static Number tryParseNumeral(String s) {
		Long l = tryParseInteger(s);
		return l != null ? l : (Number) tryParseFloat(s);
	}

	/**
	 * The '\a' character.
	 */
	public static final char CHAR_BELL = 0x07;

	/**
	 * The '\v' character.
	 */
	public static final char CHAR_VERTICAL_TAB = 0x0b;

	private static boolean isASCIIPrintable(char c) {
		// ASCII printable character range
		return c >= 32 && c < 127;
	}

	private static int shortEscape(char c) {
		switch (c) {
			case CHAR_BELL: return 'a';
			case '\b': return 'b';
			case '\f': return 'f';
			case '\n': return 'n';
			case '\r': return 'r';
			case '\t': return 't';
			case CHAR_VERTICAL_TAB: return 'v';
			default: return -1;
		}
	}

	private static char hex(int i) {
		// i must be between 0x0 and 0xf
		return i < 0xa ? (char) ((int) '0' + i) : (char) ((int) 'a' + i - 0xa);
	}

	public static String escape(CharSequence s) {
		Check.notNull(s);
		StringBuilder bld = new StringBuilder();
		bld.append('"');

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (isASCIIPrintable(c)) {
				bld.append(c);
			}
			else {
				// escaping
				bld.append('\'');

				int esc = shortEscape(c);

				if (esc != -1) {
					bld.append((char) esc);
				}
				else {
					if ((int) c <= 0xff) {
						bld.append('x');
						bld.append(hex(((int) c >>> 8) & 0xf));
						bld.append(hex((int) c & 0xf));
					}
					else {
						bld.append(Integer.toString((int) c));
					}
				}
			}
		}

		bld.append('"');
		return bld.toString();
	}

}

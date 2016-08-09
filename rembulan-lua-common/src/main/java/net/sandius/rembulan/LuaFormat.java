/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	private static int hexValue(int c) {
		if (c >= '0' && c <= '9') {
			return c - (int) '0';
		}
		else if (c >= 'a' && c <= 'f') {
			return 10 + c - (int) 'a';
		}
		else if (c >= 'A' && c <= 'F') {
			return 10 + c - (int) 'A';
		}
		else {
			return -1;
		}
	}

	public static long parseInteger(String s) throws NumberFormatException {
		s = s.trim();
		if (s.startsWith("0x") || s.startsWith("0X")) {
			long l = 0;
			int from = Math.max(2, s.length() - 16);

			for (int idx = 2; idx < from; idx++) {
				if (hexValue(s.charAt(idx)) < 0) {
					throw new NumberFormatException("Illegal character #" + idx + " in \"" + s + "\"");
				}
			}

			// only take the last 16 characters of the string for the value
			for (int idx = Math.max(2, s.length() - 16); idx < s.length(); idx++) {
				int hex = hexValue(s.charAt(idx));
				if (hex < 0) {
					throw new NumberFormatException("Illegal character #" + idx + " in \"" + s + "\"");
				}
				l = l << 4 | hex;
			}

			return l;
		}
		else {
			return Long.parseLong(s);
		}
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

	private static char toHex(int i) {
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
						bld.append(toHex(((int) c >>> 8) & 0xf));
						bld.append(toHex((int) c & 0xf));
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

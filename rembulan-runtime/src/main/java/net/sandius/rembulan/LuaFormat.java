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

import java.util.Objects;

/**
 * Static methods for parsing and generating lexical strings following the Lua lexical
 * conventions.
 */
public final class LuaFormat {

	private LuaFormat() {
		// not to be instantiated
	}

	/**
	 * String representation of <b>nil</b>.
	 */
	public static final String NIL = "nil";

	/**
	 * String representation of <b>true</b>.
	 */
	public static final String TRUE = "true";

	/**
	 * String representation of <b>false</b>.
	 */
	public static final String FALSE = "false";

	/**
	 * String representation of positive infinity.
	 */
	public static final String POS_INF = "inf";

	/**
	 * String representation of negative infinity.
	 */
	public static final String NEG_INF = "-inf";

	/**
	 * String representation of <i>NaN</i>.
	 */
	public static final String NAN = "nan";


	/**
	 * String representation of the Lua {@code nil} type.
	 *
	 * @see LuaType#NIL
	 */
	public static final String TYPENAME_NIL = NIL;

	/**
	 * String representation of the Lua {@code boolean} type.
	 *
	 * @see LuaType#BOOLEAN
	 */
	public static final String TYPENAME_BOOLEAN = "boolean";

	/**
	 * String representation of the Lua {@code number} type.
	 *
	 * @see LuaType#NUMBER
	 */
	public static final String TYPENAME_NUMBER = "number";

	/**
	 * String representation of the Lua {@code string} type.
	 *
	 * @see LuaType#STRING
	 */
	public static final String TYPENAME_STRING = "string";

	/**
	 * String representation of the Lua {@code table} type.
	 *
	 * @see LuaType#TABLE
	 */
	public static final String TYPENAME_TABLE = "table";

	/**
	 * String representation of the Lua {@code function} type.
	 *
	 * @see LuaType#FUNCTION
	 */
	public static final String TYPENAME_FUNCTION = "function";

	/**
	 * String representation of the Lua {@code userdata} type.
	 *
	 * @see LuaType#USERDATA
	 */
	public static final String TYPENAME_USERDATA = "userdata";

	/**
	 * String representation of the Lua {@code thread} type.
	 *
	 * @see LuaType#THREAD
	 */
	public static final String TYPENAME_THREAD = "thread";


	/**
	 * Returns the Lua format string representation of the boolean value {@code b}.
	 *
	 * @param b  the boolean value
	 * @return  string representation of {@code b}
	 */
	public static String toString(boolean b) {
		return b ? TRUE : FALSE;
	}

	/**
	 * Returns the Lua format string representation of the integer value {@code l}.
	 *
	 * @param l  the integer value
	 * @return  string representation of {@code l}
	 */
	public static String toString(long l) {
		return Long.toString(l);
	}

	/**
	 * Returns the Lua format string representation of the float value {@code f}.
	 *
	 * @param f  the float value
	 * @return  string representation of {@code f}
	 */
	public static String toString(double f) {
		if (Double.isNaN(f)) return NAN;
		else if (Double.isInfinite(f)) return f > 0 ? POS_INF : NEG_INF;
		else return Double.toString(f).toLowerCase();  // TODO: check precision used in Lua
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

	/**
	 * Parses the string {@code s} as an integer according to the Lua lexer rules.
	 * When {@code s} is not an integer numeral, throws a {@link NumberFormatException}.
	 *
	 * <p>This method ignores leading and trailing whitespace in {@code s}.</p>
	 *
	 * @param s  string to be parsed, must not be {@code null}
	 * @return  the integer value represented by {@code s}
	 *
	 * @throws NullPointerException  if {@code s} is {@code null}
	 * @throws NumberFormatException  if {@code s} is not a valid Lua format string representing
	 *                                an integer value
	 */
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

	/**
	 * Parses the string {@code s} as a float according to the Lua lexer rules.
	 * When {@code s} is not a float numeral, throws a {@link NumberFormatException}.
	 *
	 * <p>This method ignores leading and trailing whitespace in {@code s}.</p>
	 *
	 * @param s  the string to be parsed, must not be {@code null}
	 * @return  the float value represented by {@code s}
	 *
	 * @throws NullPointerException  if {@code s} is {@code null}
	 * @throws NumberFormatException  if {@code s} is not a valid Lua format string representing
	 *                                a float value
	 */
	public static double parseFloat(String s) throws NumberFormatException {
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

	/**
	 * Parses {@code s} as an integer following the Lua lexer rules. When {@code s} is
	 * an integer numeral, returns its value boxed as a {@link Long}. Otherwise, returns
	 * {@code null}.
	 *
	 * <p>This is a variant of {@link #parseInteger(String)} that signals invalid input
	 * by returning {@code null} rather than throwing a {@code NumberFormatException}.</p>
	 *
	 * @param s  the string to be parsed, must not be {@code null}
	 * @return  the (boxed) integer value represented by {@code s} if {@code s} is an integer
	 *          numeral; {@code null} otherwise
	 *
	 * @throws NullPointerException  if {@code s} is {@code null}
	 */
	public static Long tryParseInteger(String s) {
		try {
			return parseInteger(s);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	/**
	 * Parses {@code s} as a float following the Lua lexer rules. When {@code s} is
	 * a float numeral, returns its value boxed as a {@link Double}. Otherwise, returns
	 * {@code null}.
	 *
	 * <p>This is a variant of {@link #parseFloat(String)} that signals invalid input
	 * by returning {@code null} rather than throwing a {@code NumberFormatException}.</p>
	 *
	 * @param s  the string to be parsed, must not be {@code null}
	 * @return  the (boxed) float value represented by {@code s} if {@code s} is an float
	 *          numeral; {@code null} otherwise
	 *
	 * @throws NullPointerException  if {@code s} is {@code null}
	 */
	public static Double tryParseFloat(String s) {
		try {
			return parseFloat(s);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	/**
	 * Parses {@code s} as a number following the Lua lexer rules. When {@code s} is
	 * a numeral, returns its value boxed either as a {@link Long} (for integer numerals)
	 * or a {@link Double} (for float numerals). Otherwise, returns {@code null}.
	 *
	 * <p>Note an integer numeral is also a float numeral, but not all float numerals are
	 * integer numerals. This method returns the "most canonical" representation of the numeric
	 * value represented by {@code s}: it first tries to parse {@code s} as an integer,
	 * attempting to parse {@code s} as a float only when {@code s} is not an integer numeral.</p>
	 *
	 * @param s  the string to be parsed, must not be {@code null}
	 * @return  the numeric value represented by {@code s}, or {@code null} if {@code s}
	 *          is not a numeral
	 */
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
			case '"': return '"';
			default: return -1;
		}
	}

	private static char toHex(int i) {
		// i must be between 0x0 and 0xf
		return i < 0xa ? (char) ((int) '0' + i) : (char) ((int) 'a' + i - 0xa);
	}

	/**
	 * Returns a string {@code esc} formed from the character sequence {@code s} such that
	 * when {@code esc} is read by a Lua lexer as a string literal, it evaluates to a string equal
	 * to {@code s}. The resulting string is enclosed in double quotes ({@code "}).
	 *
	 * @param s  the character sequence to escape, must not be {@code null}
	 * @return  a Lua string literal representing {@code s}
	 *
	 * @throws NullPointerException  if {@code s} is {@code null}
	 */
	public static String escape(CharSequence s) {
		Objects.requireNonNull(s);

		StringBuilder bld = new StringBuilder();
		bld.append('"');

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c != '\\' && c != '"' && isASCIIPrintable(c)) {
				bld.append(c);
			}
			else {
				// escaping
				bld.append('\\');

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

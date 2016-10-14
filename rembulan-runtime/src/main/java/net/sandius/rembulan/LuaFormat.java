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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Static methods for parsing and generating lexical strings following the Lua lexical
 * conventions.
 */
public final class LuaFormat {

	private LuaFormat() {
		// not to be instantiated
	}

	/**
	 * The byte string representation of <b>nil</b>.
	 */
	public static final ByteString NIL = ByteString.constOf("nil");

	/**
	 * The byte string representation of <b>true</b>.
	 */
	public static final ByteString TRUE = ByteString.constOf("true");

	/**
	 * The byte string representation of <b>false</b>.
	 */
	public static final ByteString FALSE = ByteString.constOf("false");

	/**
	 * The byte string representation of infinity.
	 */
	public static final ByteString INF = ByteString.constOf("inf");

	/**
	 * The byte string representation of <i>NaN</i>.
	 */
	public static final ByteString NAN = ByteString.constOf("nan");


	/**
	 * Byte string representation of the Lua {@code nil} type.
	 */
	public static final ByteString TYPENAME_NIL = NIL;

	/**
	 * Byte string representation of the Lua {@code boolean} type.
	 */
	public static final ByteString TYPENAME_BOOLEAN = ByteString.constOf("boolean");

	/**
	 * Byte string representation of the Lua {@code number} type.
	 */
	public static final ByteString TYPENAME_NUMBER = ByteString.constOf("number");

	/**
	 * Byte string representation of the Lua {@code string} type.
	 */
	public static final ByteString TYPENAME_STRING = ByteString.constOf("string");

	/**
	 * Byte string representation of the Lua {@code table} type.
	 */
	public static final ByteString TYPENAME_TABLE = ByteString.constOf("table");

	/**
	 * Byte string representation of the Lua {@code function} type.
	 */
	public static final ByteString TYPENAME_FUNCTION = ByteString.constOf("function");

	/**
	 * Byte string representation of the Lua {@code userdata} type.
	 */
	public static final ByteString TYPENAME_USERDATA = ByteString.constOf("userdata");

	/**
	 * Byte string representation of the Lua {@code thread} type.
	 */
	public static final ByteString TYPENAME_THREAD = ByteString.constOf("thread");

	/**
	 * Returns the Lua format string representation of the boolean value {@code b}.
	 *
	 * <p><b>Note:</b> this method returns a {@code java.lang.String}. In order to
	 * obtain a <i>byte</i> string, use {@link #toByteString(boolean)} rather than
	 * wrapping the result of this method using {@link ByteString#of(String)}.</p>
	 *
	 * @param b  the boolean value
	 * @return  string representation of {@code b}
	 */
	public static String toString(boolean b) {
		return toByteString(b).toString();
	}

	/**
	 * Returns the Lua format byte string representation of the boolean value {@code b}
	 * as a byte string.
	 *
	 * @param b  the boolean value
	 * @return  byte string representation of {@code b}
	 */
	public static ByteString toByteString(boolean b) {
		return b ? TRUE : FALSE;
	}

	/**
	 * Returns the Lua format string representation of the integer value {@code l}.
	 *
	 * <p><b>Note:</b> this method returns a {@code java.lang.String}. In order to
	 * obtain a <i>byte</i> string, use {@link #toByteString(long)} rather than
	 * wrapping the result of this method using {@link ByteString#of(String)}.</p>
	 *
	 * @param l  the integer value
	 * @return  string representation of {@code l}
	 */
	public static String toString(long l) {
		return Long.toString(l);
	}

	/**
	 * Returns the Lua format byte string representation of the integer value {@code l}.
	 *
	 * @param l  the integer value
	 * @return  byte string representation of {@code l}
	 */
	public static ByteString toByteString(long l) {
		return ByteString.of(toString(l));
	}

	/**
	 * Returns the Lua format string representation of the float value {@code f}.
	 *
	 * <p><b>Note:</b> this method returns a {@code java.lang.String}. In order to
	 * obtain a <i>byte</i> string, use {@link #toByteString(long)} rather than
	 * wrapping the result of this method using {@link ByteString#of(String)}.</p>
	 *
	 * @param f  the float value
	 * @return  string representation of {@code f}
	 */
	public static String toString(double f) {
		return toByteString(f).toString();
	}

	private static ByteString finiteDoubleToByteString(double f) {
		// f assumed not to be NaN or infinite
		// TODO: check precision used in Lua
		// TODO: don't go via java.lang.String
		String s = Double.toString(f).toLowerCase();
		return ByteString.of(s);
	}

	private static final ByteString NEG_INF = ByteString.constOf("-" + INF);

	/**
	 * Returns the Lua format byte string representation of the float value {@code f}.
	 *
	 * @param f  the float value
	 * @return  byte string representation of {@code f}
	 */
	public static ByteString toByteString(double f) {
		if (Double.isNaN(f)) return NAN;
		else if (Double.isInfinite(f)) return f > 0 ? INF : NEG_INF;
		else return finiteDoubleToByteString(f);
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

	/**
	 * Returns a string {@code esc} formed from the byte string {@code s} such that
	 * when {@code esc} is read by a Lua lexer as a string literal, it evaluates to
	 * a byte string equal to {@code s}. The resulting string is enclosed in double quotes
	 * ({@code "}).
	 *
	 * @param byteString  the byte sequence sequence to escape, must not be {@code null}
	 * @return  a Lua string literal representing {@code s}
	 *
	 * @throws NullPointerException  if {@code s} is {@code null}
	 */
	public static String escape(ByteString byteString) {
		return escape(byteString.toRawString());
	}

	private static final Set<String> keywords;

	static {
		Set<String> ks = new HashSet<>();
		Collections.addAll(ks,
				"and", "break", "do", "else", "elseif", "end", "false", "for",
				"function", "goto", "if", "in", "local", "nil", "not", "or",
				"repeat", "return", "then", "true", "until", "while");
		keywords = Collections.unmodifiableSet(ks);
	}

	/**
	 * Returns {@code true} iff the string {@code s} is a keyword in Lua.
	 * 
	 * <p>A keyword in Lua is one of the following strings:
	 * {@code "and"}, {@code "break"}, {@code "do"}, {@code "else"}, {@code "elseif"},
	 * {@code "end"}, {@code "false"}, {@code "for"}, {@code "function"}, {@code "goto"},
	 * {@code "if"}, {@code "in"}, {@code "local"}, {@code "nil"}, {@code "not"},
	 * {@code "or"}, {@code "repeat"}, {@code "return"}, {@code "then"}, {@code "true"},
	 * {@code "until"}, {@code "while"}.</p>
	 *
	 * @param s  the string to be examined, may be {@code null}
	 * @return  {@code true} if {@code s} is a Lua keyword; {@code false} otherwise
	 */
	public static boolean isKeyword(String s) {
		return s != null && keywords.contains(s);
	}

	/**
	 * Returns {@code true} iff the string {@code s} is a valid Lua name.
	 *
	 * <p>According to §3.1 of the Lua Reference Manual,</p>
	 *
	 * <blockquote>
	 *     Names (also called identifiers) in Lua can be any string of letters, digits,
	 *     and underscores, not beginning with a digit and not being a reserved word.
	 * </blockquote>
	 *
	 * <p>This implementation treats letters as characters in the ranges
	 * {@code 'a'}...{@code 'z'} and {@code 'A'}...{@code 'Z'}, and numbers as characters in
	 * the range {@code '0'}...{@code '9'}.</p>
	 *
	 * @param s  the string to be checked for being a valid name, may be {@code null}
	 * @return  {@code true} if {@code s} is a valid name in Lua; {@code false} otherwise
	 */
	public static boolean isValidName(String s) {
		if (s == null || s.isEmpty() || isKeyword(s)) {
			return false;
		}

		char c = s.charAt(0);

		if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c != '_')) return false;

		for (int i = 1; i < s.length(); i++) {
			c = s.charAt(i);
			if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c != '_') && (c < '0' || c > '9')) return false;
		}

		return true;
	}


}

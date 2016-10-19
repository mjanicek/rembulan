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
 *
 * --
 * Portions of this file are licensed under the Lua license. For Lua
 * licensing details, please visit
 *
 *     http://www.lua.org/license.html
 *
 * Copyright (C) 1994-2016 Lua.org, PUC-Rio.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Patterns in Lua are described by regular strings, which are interpreted as patterns
 * by the pattern-matching functions {@link DefaultStringLib#FIND {@code string.find}},
 * {@link DefaultStringLib#GMATCH {@code string.gmatch}},
 * {@link DefaultStringLib#GSUB {@code string.gsub}},
 * and {@link DefaultStringLib#MATCH {@code string.match}}.
 * This section describes the syntax and the meaning (that is, what they match) of these
 * strings.
 *
 * <h2>Character Class:</h2>
 *
 * <p>A <i>character class</i> is used to represent a set of characters. The following
 * combinations are allowed in describing a character class:</p>
 *
 * <ul>
 *   <li><b><i>x</i></b>: (where <i>x</i> is not one of the <i>magic characters</i>
 *     {@code ^$()%.[]*+-?}) represents the character <i>x</i> itself.</li>
 *   <li><b>{@code .}</b>: (a dot) represents all characters.</li>
 *   <li><b>{@code %a}</b>: represents all letters.</li>
 *   <li><b>{@code %c}</b>: represents all control characters.</li>
 *   <li><b>{@code %d}</b>: represents all digits.</li>
 *   <li><b>{@code %g}</b>: represents all printable characters except space.</li>
 *   <li><b>{@code %l}</b>: represents all lowercase letters.</li>
 *   <li><b>{@code %p}</b>: represents all punctuation characters.</li>
 *   <li><b>{@code %s}</b>: represents all space characters.</li>
 *   <li><b>{@code %u}</b>: represents all uppercase letters.</li>
 *   <li><b>{@code %w}</b>: represents all alphanumeric characters.</li>
 *   <li><b>{@code %x}</b>: represents all hexadecimal digits.</li>
 *   <li><b><code>%<i>x</i></code></b>: (where <i>x</i> is any non-alphanumeric character)
 *     represents the character <i>x</i>. This is the standard way to escape the magic characters.
 *     Any non-alphanumeric character (including all punctuation characters, even the non-magical)
 *     can be preceded by a {@code '%'} when used to represent itself in a pattern.</li>
 *   <li><b><code>[<i>set</i>]</code></b>: represents the class which is the union
 *     of all characters in set. A range of characters can be specified by separating the end
 *     characters of the range, in ascending order, with a {@code '-'}. All classes
 *     <code>%<i>x</i></code> described above can also be used as components in <i>set</i>.
 *     All other characters in set represent themselves. For example, {@code [%w_]}
 *     (or {@code [_%w]}) represents all alphanumeric characters plus the underscore,
 *     {@code [0-7]} represents the octal digits, and {@code [0-7%l%-]} represents the octal
 *     digits plus the lowercase letters plus the {@code '-'} character.
 *
 *     <p>You can put a closing square bracket in a set by positioning it as the first character
 *     in the set. You can put an hyphen in a set by positioning it as the first or the last
 *     character in the set. (You can also use an escape for both cases.)</p>
 *
 *     <p>The interaction between ranges and classes is not defined. Therefore, patterns like
 *     {@code [%a-z]} or {@code [a-%%]} have no meaning.</p></li>
 *
 *   <li><b><code>[^<i>set</i>]</code></b>: represents the complement of <i>set</i>,
 *     where <i>set</i> is interpreted as above.</li>
 * </ul>
 *
 * <p>For all classes represented by single letters ({@code %a}, {@code %c}, etc.),
 * the corresponding uppercase letter represents the complement of the class. For instance,
 * {@code %S} represents all non-space characters.</p>
 *
 * The definitions of letter, space, and other character groups depend on the current locale.
 * In particular, the class {@code [a-z]} may not be equivalent to {@code %l}.
 *
 * <h2>Pattern Item:</h2>
 *
 * <p>A <i>pattern item</i> can be</p>
 *
 * <ul>
 *   <li>a single character class, which matches any single character in the class;</li>
 *   <li>a single character class followed by {@code '*'}, which matches zero or more repetitions
 *     of characters in the class. These repetition items will always match the longest possible
 *     sequence;</li>
 *   <li>a single character class followed by {@code '+'}, which matches one or more repetitions
 *     of characters in the class. These repetition items will always match the longest possible
 *     sequence;</li>
 *   <li>a single character class followed by {@code '-'}, which also matches zero or more
 *     repetitions of characters in the class. Unlike {@code '*'}, these repetition items will
 *     always match the shortest possible sequence;</li>
 *   <li>a single character class followed by {@code '?'}, which matches zero or one occurrence
 *     of a character in the class. It always matches one occurrence if possible;</li>
 *   <li><code>%<i>n</i></code>, for <i>n</i> between 1 and 9; such item matches a substring
 *     equal to the <i>n</i>-th captured string (see below);</li>
 *   <li><code>%b<i>xy</i></code>, where <i>x</i> and <i>y</i> are two distinct characters;
 *     such item matches strings that start with <i>x</i>, end with <i>y</i>, and where the
 *     <i>x</i> and <i>y</i> are <i>balanced</i>. This means that, if one reads the string from
 *     left to right, counting +1 for an <i>x</i> and -1 for a <i>y</i>, the ending <i>y</i>
 *     is the first <i>y</i> where the count reaches 0. For instance, the item {@code %b()}
 *     matches expressions with balanced parentheses.</li>
 *   <li><code>%f[<i>set</i>]</code>, a <i>frontier pattern</i>; such item matches an empty string
 *     at any position such that the next character belongs to <i>set</i> and the previous
 *     character does not belong to set. The set <i>set</i> is interpreted as previously
 *     described. The beginning and the end of the subject are handled as if they were
 *     the character {@code '\0'}.</li>
 * </ul>
 *
 * <h2>Pattern:</h2>
 *
 * <p>A <i>pattern</i> is a sequence of pattern items. A caret {@code '^'} at the beginning
 * of a pattern anchors the match at the beginning of the subject string. A {@code '$'}
 * at the end of a pattern anchors the match at the end of the subject string. At other positions,
 * {@code '^'} and {@code '$'} have no special meaning and represent themselves.</p>
 *
 * <h2>Captures:</h2>
 *
 * <p>A pattern can contain sub-patterns enclosed in parentheses; they describe <i>captures</i>.
 * When a match succeeds, the substrings of the subject string that match captures are stored
 * (<i>captured</i>) for future use. Captures are numbered according to their left parentheses.
 * For instance, in the pattern {@code "(a*(.)%w(%s*))"}, the part of the string matching
 * {@code "a*(.)%w(%s*)"} is stored as the first capture (and therefore has number 1);
 * the character matching {@code "."} is captured with number 2, and the part matching
 * {@code "%s*"} has number 3.</p>
 *
 * <p>As a special case, the empty capture {@code ()} captures the current string position
 * (a number). For instance, if we apply the pattern {@code "()aa()"} on the string
 * {@code "flaaap"}, there will be two captures: 3 and 5.</p>
 */
public class StringPattern {

	private final List<PI> items;
	private final int numCaptures;

	private StringPattern(
			List<PI> items,
			int numCaptures) {

		this.items = Check.notNull(items);
		this.numCaptures = Check.nonNegative(numCaptures);
	}

	private static final String MAGIC_CHARS = "^$()%.[]*+-?";
	private static final String PUNCTUATION_CHARS = ".,;:?!";

	private static boolean isMagic(char c) {
		return MAGIC_CHARS.indexOf(c) != -1;
	}

	public static class Match {

		private final String originalString;
		private final int beginIndex;
		private final int endIndex;
		private final List<Object> captures;

		protected Match(String originalString, int beginIndex, int endIndex, List<Object> captures) {
			this.originalString = Check.notNull(originalString);
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.captures = Check.notNull(captures);
		}

		public String originalString() {
			return originalString;
		}

		public int beginIndex() {
			return beginIndex;
		}

		public int endIndex() {
			return endIndex;
		}

		public String fullMatch() {
			return originalString.substring(beginIndex, endIndex);
		}

		public List<Object> captures() {
			return captures;
		}

	}

	// returns null to signal no-match
	public Match match(String s, int fromIndex) {
		// iterate one character beyond the last one: empty matches succeed at EOS
		while (fromIndex >= 0 && fromIndex <= s.length()) {
			MatchState ms = new MatchState(items, s, fromIndex);
			int result = ms.start();
			if (result != NO_MATCH) {
				// got a match
				return new Match(s, fromIndex, result, Collections.unmodifiableList(Arrays.asList(ms.cap)));
			}
			else {
				// no match: skip the first character and try again
				fromIndex += 1;
			}
		}

		// no match
		return null;
	}

	static class CharacterSet {

		private final List<SetElement> elements;

		CharacterSet(List<SetElement> elements) {
			this.elements = Check.notNull(elements);
		}

		@Override
		public String toString() {
			StringBuilder bld = new StringBuilder();
			for (SetElement element : elements) {
				bld.append(element);
			}
			return bld.toString();
		}

		public boolean matches(char c) {
			for (SetElement elem : elements) {
				if (elem.matches(c)) {
					return true;
				}
			}
			return false;
		}

		static abstract class SetElement {

			public abstract boolean matches(char c);

		}

		static class RangeSetElement extends SetElement {

			private final char min;
			private final char max;

			RangeSetElement(char min, char max) {
				this.min = min;
				this.max = max;
			}

			@Override
			public String toString() {
				return min + "-" + max;
			}

			@Override
			public boolean matches(char c) {
				return c >= min && c <= max;
			}

		}

		static class CharacterClassSetElement extends SetElement {

			private final CC ccl;

			CharacterClassSetElement(CC ccl) {
				this.ccl = Check.notNull(ccl);
			}

			@Override
			public String toString() {
				return ccl.toString();
			}

			@Override
			public boolean matches(char c) {
				return ccl.matches(c);
			}

		}

	}

	static abstract class CC {

		public abstract boolean matches(int c);

	}

	static class CC_lit extends CC {

		private final char ch;

		CC_lit(char ch) {
			this.ch = ch;
		}

		@Override
		public String toString() {
			return (isMagic(ch) ? "%" : "") + Character.toString(ch);
		}

		@Override
		public boolean matches(int c) {
			return c >= 0 && ch == (char) c;
		}

	}

	static class CC_spec extends CC {

		enum ClassDesc {

			ALL("."),  // .
			LETTERS("%a"),  // %a
			LOWERCASE_LETTERS("%l"),  // %l
			UPPERCASE_LETTERS("%u"),  // %u
			DECIMAL_DIGITS("%d"),  // %d
			HEXADECIMAL_DIGITS("%x"),  // %x
			ALPHANUMERIC("%w"),  // %w
			SPACE("%s"),  // %s
			CONTROL_CHARS("%c"),  // %c
			PUNCTUATION("%p"),  // %p
			PRINTABLE_EXCEPT_SPACE("%g");  // %g

			private final String s;

			ClassDesc(String s) {
				this.s = s;
			}

			@Override
			public String toString() {
				return s;
			}

		}

		private final ClassDesc desc;
		private final boolean complement;

		CC_spec(ClassDesc desc, boolean complement) {
			Check.isFalse(desc == ClassDesc.ALL && complement);
			this.desc = Check.notNull(desc);
			this.complement = complement;
		}

		@Override
		public String toString() {
			String s = desc.toString();
			return complement ? s.toUpperCase() : s;
		}

		@Override
		public boolean matches(int c) {
			if (c >= 0) {
				if (desc == ClassDesc.ALL) {
					return true;
				}
				else {
					char ch = (char) c;
					switch (desc) {
						case LETTERS:
							return complement != Character.isLetter(ch);
						case LOWERCASE_LETTERS:
							return complement != (Character.isLetter(ch) && Character.isLowerCase(ch));
						case UPPERCASE_LETTERS:
							return complement != (Character.isLetter(ch) && Character.isUpperCase(ch));
						case DECIMAL_DIGITS:
							return complement != (ch >= '0' && ch <= '9');
						case HEXADECIMAL_DIGITS:
							return complement != ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F'));
						case ALPHANUMERIC: {
							char lc = Character.toLowerCase(ch);
							return complement != ((lc >= '0' && lc <= '9') || (lc >= 'a' && lc <= 'z'));
						}
						case SPACE:
							return complement != Character.isWhitespace(ch);
						case CONTROL_CHARS:
							return complement != Character.isISOControl(ch);
						case PUNCTUATION:
							return complement != (PUNCTUATION_CHARS.indexOf(ch) != -1);
						case PRINTABLE_EXCEPT_SPACE:
							return complement != (!Character.isISOControl(ch) && ch != ' ');

						default: throw new IllegalStateException();
					}
				}
			}
			else {
				return false;
			}
		}

	}

	static class CC_set extends CC {

		private final CharacterSet cs;
		private final boolean complement;

		CC_set(CharacterSet cs, boolean complement) {
			this.cs = Check.notNull(cs);
			this.complement = complement;
		}

		@Override
		public String toString() {
			return (complement ? "[^" : "[") + cs.toString() + "]";
		}

		@Override
		public boolean matches(int c) {
			return c >= 0 && complement != cs.matches((char) c);
		}

	}

	enum Repeat {

		EXACTLY_ONCE(""),  // no modifier
		LONGEST_ZERO_OR_MORE("*"),  // *
		SHORTEST_ZERO_OR_MORE("-"),  // -
		ONE_OR_MORE("+"),  // +
		AT_MOST_ONCE("?");  // ?

		private final String s;

		Repeat(String s) {
			this.s = s;
		}

		@Override
		public String toString() {
			return s;
		}

	}

	class MatchState {
		private final int piIdx;
		private final List<PI> pis;
		private final String str;
		private final int strIdx;
		private final int[] capBegin;
		private final Object[] cap;

		MatchState(int piIdx, List<PI> pis, String str, int strIdx, int[] capBegin, Object[] cap) {
			this.piIdx = piIdx;
			this.pis = pis;
			this.str = str;
			this.strIdx = strIdx;
			this.capBegin = capBegin;
			this.cap = cap;
		}

		MatchState(List<PI> pis, String str, int strIdx) {
			this(0, pis, str, strIdx, new int[numCaptures], new Object[numCaptures]);
		}

		private PI pi() {
			return pis.get(piIdx);
		}

		private MatchState nextState(int strIdx) {
			if (piIdx + 1 < pis.size()) {
				return new MatchState(piIdx + 1, pis, str, strIdx, capBegin, cap);
			}
			else {
				return null;
			}
		}

		public int start() {
			if (pis.isEmpty()) {
				return strIdx;
			}
			else {
				PI pi = pis.get(0);
				return pi.match(this);
			}
		}

		public int next(int strIdx) {
			MatchState ms = nextState(strIdx);
			if (ms != null) {
				return ms.pi().match(ms);
			}
			else {
				return strIdx;
			}
		}

		public int peek(int pos) {
			return pos >= 0 && pos < str.length() ? str.charAt(pos) : -1;
		}

		public int peek() {
			return peek(strIdx);
		}

	}

	static abstract class PI {

		public abstract int match(MatchState ms);

	}

	private static final int NO_MATCH = -1;

	private static final PI_begin PI_BEGIN = new PI_begin();
	private static final PI_eos PI_EOS = new PI_eos();

	static class PI_begin extends PI {

		@Override
		public String toString() {
			return "^";
		}

		@Override
		public int match(MatchState ms) {
			if (ms.strIdx == 0) {
				return ms.next(ms.strIdx);
			}
			else {
				return NO_MATCH;
			}
		}

	}

	static class PI_eos extends PI {

		@Override
		public String toString() {
			return "$";
		}

		@Override
		public int match(MatchState ms) {
			if (ms.strIdx == ms.str.length() - 1) {
				return ms.next(ms.strIdx);
			}
			else {
				return NO_MATCH;
			}
		}

	}

	static class PI_cc extends PI {

		private final CC ccl;
		private final Repeat mod;

		PI_cc(CC ccl, Repeat mod) {
			this.ccl = Check.notNull(ccl);
			this.mod = Check.notNull(mod);
		}

		@Override
		public String toString() {
			return ccl.toString() + mod.toString();
		}

		@Override
		public int match(MatchState ms) {
			switch (mod) {

				case EXACTLY_ONCE: {
					int c = ms.peek();
					if (ccl.matches(c)) {
						return ms.next(ms.strIdx + 1);
					}
					else {
						return NO_MATCH;
					}
				}

				case LONGEST_ZERO_OR_MORE: {
					final int min = ms.strIdx;
					final int max;

					{
						int i = min;
						while (ccl.matches(ms.peek(i))) {
							i++;
						}
						max = i;
					}

					for (int j = max; j >= min; j--) {
						int nxt = ms.next(j);
						if (nxt != NO_MATCH) {
							return nxt;
						}
					}

					return NO_MATCH;
				}

				case SHORTEST_ZERO_OR_MORE: {
					final int min = ms.strIdx;
					final int max;

					{
						int i = min;
						while (ccl.matches(ms.peek(i))) {
							i++;
						}
						max = i;
					}

					for (int j = min; j < max; j++) {
						int nxt = ms.next(j);
						if (nxt != NO_MATCH) {
							return nxt;
						}
					}

					return NO_MATCH;
				}

				case ONE_OR_MORE: {
					int c = ms.peek();
					if (ccl.matches(c)) {
						final int min = ms.strIdx + 1;
						final int max;

						{
							int i = min;
							while (ccl.matches(ms.peek(i))) {
								i++;
							}
							max = i;
						}

						for (int j = max; j >= min; j--) {
							int nxt = ms.next(j);
							if (nxt != NO_MATCH) {
								return nxt;
							}
						}

						return NO_MATCH;
					}
					else {
						return NO_MATCH;
					}
				}

				case AT_MOST_ONCE: {
					int c = ms.peek();
					if (ccl.matches(c)) {
						int nxt = ms.next(ms.strIdx + 1);
						if (nxt != NO_MATCH) {
							return nxt;
						}
						// else fall through and try without this character
					}

					return ms.next(ms.strIdx);
				}

				default:
					// should not happen
					throw new IllegalStateException();
			}
		}

	}

	// %1, %2, ..., %9
	static class PI_cmatch extends PI {

		private final int index;

		PI_cmatch(int index) {
			this.index = Check.inRange(index, 1, 9);
		}

		@Override
		public String toString() {
			return "%" + index;
		}

		@Override
		public int match(MatchState ms) {
			Object o = ms.cap[index-1];

			if (o instanceof String) {
				String cs = (String) o;
				int offset = ms.strIdx;
				for (int i = 0; i < cs.length(); i++) {
					if (offset + i >= ms.str.length()) {
						// EOS
						return NO_MATCH;
					}

					if (ms.str.charAt(offset + i) != cs.charAt(i)) {
						// non-matching character
						return NO_MATCH;
					}
				}

				return ms.next(offset + cs.length());
			}
			else {
				// don't match positions
				return NO_MATCH;
			}
		}

	}

	// %bxy
	static class PI_balanced extends PI {

		private final char first;
		private final char second;

		PI_balanced(char first, char second) {
			Check.isTrue(first != second);
			this.first = first;
			this.second = second;
		}

		@Override
		public String toString() {
			return "%b" + first + second;
		}

		@Override
		public int match(MatchState ms) {
			int idx = ms.strIdx;

			if (idx >= ms.str.length() || ms.str.charAt(idx) != first) {
				return NO_MATCH;
			}

			int balance = 0;
			while (idx < ms.str.length()) {
				char c = ms.str.charAt(idx);
				if (c == first) {
					balance += 1;
				}
				else if (c == second) {
					balance -= 1;
				}

				idx++;

				if (balance == 0) {
					int nxt = ms.next(idx);
					if (nxt != NO_MATCH) {
						// we're done
						return nxt;
					}
					else {
						// continue
					}
				}
			}

			// not balanced
			return NO_MATCH;
		}

	}

	// %f[set]
	static class PI_frontier extends PI {

		private final CharacterSet cs;

		PI_frontier(CharacterSet cs) {
			this.cs = Check.notNull(cs);
		}

		@Override
		public String toString() {
			return "%f[" + cs.toString() + "]";
		}

		@Override
		public int match(MatchState ms) {
			if (ms.strIdx > 0 && ms.strIdx < ms.str.length()) {
				char c = ms.str.charAt(ms.strIdx - 1);
				char d = ms.str.charAt(ms.strIdx);

				if (!cs.matches(c) && cs.matches(d)) {
					return ms.next(ms.strIdx);
				}
				else {
					return NO_MATCH;
				}
			}
			else {
				return NO_MATCH;
			}
		}

	}

	// (pattern)
	static abstract class PI_capture extends PI {

		protected final int index;

		PI_capture(int index) {
			this.index = Check.positive(index);
		}

	}

	static class PI_capture_pos extends PI_capture {

		PI_capture_pos(int index) {
			super(index);
		}

		@Override
		public String toString() {
			return "()";
		}

		@Override
		public int match(MatchState ms) {
			ms.cap[index - 1] = Long.valueOf(ms.strIdx + 1);
			return ms.next(ms.strIdx);
		}

	}

	static class PI_capture_begin extends PI_capture {

		PI_capture_begin(int index) {
			super(index);
		}

		@Override
		public String toString() {
			return "(";
		}

		@Override
		public int match(MatchState ms) {
			ms.capBegin[index - 1] = ms.strIdx;
			return ms.next(ms.strIdx);
		}

	}

	static class PI_capture_end extends PI_capture {

		PI_capture_end(int index) {
			super(index);
		}

		@Override
		public String toString() {
			return ")";
		}

		@Override
		public int match(MatchState ms) {
			int endIdx = Math.min(ms.str.length(), ms.strIdx);
			String s = ms.str.substring(ms.capBegin[index - 1], endIdx);
			ms.cap[index - 1] = s;
			return ms.next(ms.strIdx);
		}

	}

	static class PatternBuilder {

		private final String pattern;
		private final boolean anchoredBegin;
		private int index;
		private int nextCaptureIndex;
		private Set<Integer> assignedCaptures;

		PatternBuilder(String pattern, boolean ignoreCaret) {
			final boolean anchoredBegin;

			if (pattern.startsWith("^")) {
				pattern = pattern.substring(1);
				anchoredBegin = !ignoreCaret;
			}
			else {
				anchoredBegin = false;
			}

			this.pattern = Check.notNull(pattern);
			this.anchoredBegin = anchoredBegin;

			this.index = 0;
			this.nextCaptureIndex = 1;
			assignedCaptures = new HashSet<>();
		}

		private static RuntimeException parseError(int index, String message) {
			return new IllegalArgumentException("error at character " + index + ": " + message);
		}

		private char peek() {
			if (index < pattern.length()) {
				return pattern.charAt(index);
			}
			else {
				throw parseError(index, "unexpected <eos>");
			}
		}

		private String pretty(int idx) {
			// assuming idx >= 0
			return idx < pattern.length() ? "'" + pattern.charAt(idx) + "'" : "<eos>";
		}

		private boolean isEos() {
			return index >= pattern.length();
		}

		private void consume(String s) {
			for (int i = 0; i < s.length(); i++) {
				consume(s.charAt(i));
			}
		}

		private void consume(char c) {
			if (index < pattern.length() && pattern.charAt(index) == c) {
				index += 1;
			}
			else {
				throw parseError(index, "expected '" + c + "', got " + pretty(index));
			}
		}

		private char next() {
			char c = peek();
			index++;
			return c;
		}

		private void skip(int offset) {
			index += offset;
		}

		private Repeat repeat() {
			if (!isEos()) {
				char d = peek();
				switch (d) {
					case '+': skip(1); return Repeat.ONE_OR_MORE;
					case '*': skip(1); return Repeat.LONGEST_ZERO_OR_MORE;
					case '-': skip(1); return Repeat.SHORTEST_ZERO_OR_MORE;
					case '?': skip(1); return Repeat.AT_MOST_ONCE;
				}
			}

			return Repeat.EXACTLY_ONCE;
		}

		private CharacterSet.SetElement characterSetElement() {
			CC ccl = tryEscapedCC();
			if (ccl != null) {
				return new CharacterSet.CharacterClassSetElement(ccl);
			}
			else {
				// not escaped
				char c = next();
				if (peek() == '-') {
					// it's a range
					consume("-");
					char d = next();
					return new CharacterSet.RangeSetElement(c, d);
				}
				else {
					// not a range
					return new CharacterSet.CharacterClassSetElement(CC_lit(c));
				}
			}
		}

		private CharacterSet characterSetBody() {
			List<CharacterSet.SetElement> elems = new ArrayList<>();
			while (!isEos() && peek() != ']') {
				elems.add(characterSetElement());
			}

			if (elems.isEmpty()) {
				throw parseError(index, "empty character set");
			}

			return new CharacterSet(Collections.unmodifiableList(elems));
		}

		private void PI_frontier(List<PI> pis) {
			consume("%f[");
			CharacterSet cs = characterSetBody();
			consume("]");
			pis.add(new PI_frontier(cs));
		}

		private void PI_balanced(List<PI> pis) {
			consume("%b");
			char x = next();
			char y = next();
			if (x == y) {
				throw parseError(index, "x == y in %bxy");
			}
			pis.add(new PI_balanced(x, y));
		}

		private void PI_cmatch(List<PI> pis) {
			consume("%");
			char c = next();
			if (c >= '1' && c <= '9') {
				int cidx = (int) c - (int) '0';
				// "(()%1)" and "()%2" should be rejected, but "(()%2)" is ok
				if (!assignedCaptures.contains(cidx)) {
					throw parseError(index, "capture #" + cidx + " not resolved at this point");
				}
				else {
					pis.add(new PI_cmatch(cidx));
				}
			}
			else {
				throw parseError(index, "expected '1'..'9', got " + pretty(index));
			}
		}

		private int charAtOffset(int idx) {
			int j = index + idx;
			return j >= 0 && j < pattern.length() ? pattern.charAt(j) : -1;
		}

		private boolean continuesWith(String s) {
			for (int i = 0; i < s.length(); i++) {
				int j = index + i;
				if (j >= pattern.length() || s.charAt(i) != pattern.charAt(j)) {
					return false;
				}
			}
			return true;
		}

		private CC_lit CC_lit(char c) {
			if (isMagic(c)) {
				throw parseError(index, "unexpected character '" + c + "'");
			}
			return new CC_lit(c);
		}

		private static CC_spec.ClassDesc maybeClassDesc(int c) {
			switch (c) {
				case 'a': return CC_spec.ClassDesc.LETTERS;
				case 'c': return CC_spec.ClassDesc.CONTROL_CHARS;
				case 'd': return CC_spec.ClassDesc.DECIMAL_DIGITS;
				case 'g': return CC_spec.ClassDesc.PRINTABLE_EXCEPT_SPACE;
				case 'l': return CC_spec.ClassDesc.LOWERCASE_LETTERS;
				case 'p': return CC_spec.ClassDesc.PUNCTUATION;
				case 's': return CC_spec.ClassDesc.SPACE;
				case 'u': return CC_spec.ClassDesc.UPPERCASE_LETTERS;
				case 'w': return CC_spec.ClassDesc.ALPHANUMERIC;
				case 'x': return CC_spec.ClassDesc.HEXADECIMAL_DIGITS;
				default: return null;
			}
		}

		private CC tryEscapedCC() {
			if (continuesWith("%")) {
				int o = charAtOffset(1);
				int lo = Character.toLowerCase(o);
				CC_spec.ClassDesc cd = maybeClassDesc(lo);
				if (cd != null) {
					consume("%");
					skip(1);
					return new CC_spec(cd, lo != o);
				}
				else {
					consume("%");
					char c = next();
					return new CC_lit(c);
				}
			}
			else {
				return null;
			}
		}

		private CC cclass() {

			if (continuesWith("[^")) {
				consume("[^");
				CharacterSet cs = characterSetBody();
				consume("]");
				return new CC_set(cs, true);
			}

			if (continuesWith("[")) {
				consume("[");
				CharacterSet cs = characterSetBody();
				consume("]");
				return new CC_set(cs, false);
			}


			CC ccl = tryEscapedCC();

			if (ccl != null) {
				return ccl;
			}
			else {
				char c = next();
				if (c == '.') {
					return new CC_spec(CC_spec.ClassDesc.ALL, false);
				}
				else {
					return CC_lit(c);
				}
			}
		}

		private void PI_cc(List<PI> pis) {
			CC ccl = cclass();
			Repeat mod = repeat();
			pis.add(new PI_cc(ccl, mod));
		}

		private void PI(List<PI> pis) {
			if (continuesWith("(")) {
				PI_capture(pis);
			}
			else if (continuesWith("%f[")) {
				PI_frontier(pis);
			}
			else if (continuesWith("%b")) {
				PI_balanced(pis);
			}
			else if (continuesWith("%") && charAtOffset(1) >= (int) '1' && charAtOffset(1) <= (int) '9') {
				PI_cmatch(pis);
			}
			else if (continuesWith("$") && charAtOffset(1) == -1) {
				skip(1);
				pis.add(PI_EOS);
			}
			else {
				PI_cc(pis);
			}
		}

		private void PI_capture(List<PI> pis) {
			int capIdx = nextCaptureIndex++;

			consume('(');
			List<PI> nested = new ArrayList<>();
			while (!isEos() && peek() != ')') {
				PI(nested);
			}
			consume(')');

			assignedCaptures.add(capIdx);

			if (nested.isEmpty()) {
				pis.add(new PI_capture_pos(capIdx));
			}
			else {
				pis.add(new PI_capture_begin(capIdx));
				pis.addAll(nested);
				pis.add(new PI_capture_end(capIdx));
			}
		}

		private StringPattern parse() {
			List<PI> items = new ArrayList<>();
			if (anchoredBegin) {
				items.add(PI_BEGIN);
			}
			while (!isEos()) {
				PI(items);
			}
			items = Collections.unmodifiableList(items);

			return new StringPattern(items, nextCaptureIndex - 1);
		}

	}

	public static StringPattern fromString(String pattern, boolean ignoreCaret) {
		return new PatternBuilder(pattern, ignoreCaret).parse();
	}

	public static StringPattern fromString(String pattern) {
		return fromString(pattern, false);
	}

	private static String listOfPIToString(List<PI> items) {
		StringBuilder builder = new StringBuilder();
		for (PI pi : items) {
			builder.append(pi.toString());
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return listOfPIToString(items);
	}

}

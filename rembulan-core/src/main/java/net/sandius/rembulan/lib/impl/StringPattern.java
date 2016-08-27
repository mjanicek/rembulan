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

import net.sandius.rembulan.lib.StringLib;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Patterns in Lua are described by regular strings, which are interpreted as patterns
 * by the pattern-matching functions {@link StringLib#_find() {@code string.find}},
 * {@link StringLib#_gmatch() {@code string.gmatch}},
 * {@link StringLib#_gsub() {@code string.gsub}},
 * and {@link StringLib#_match() {@code string.match}}.
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

	private final List<PatternItem> items;
	private final boolean anchoredBegin;

	private StringPattern(
			List<PatternItem> items,
			boolean anchoredBegin) {

		this.items = Check.notNull(items);
		this.anchoredBegin = anchoredBegin;
	}

	private static final String MAGIC = "^$()%.[]*+-?";

	private static boolean isMagic(char c) {
		return MAGIC.indexOf(c) != -1;
	}

	public interface MatchAction {
		void onMatch(String s, int firstIndex, int lastIndex);
		// if value == null, it's just the index
		void onCapture(String s, int index, String value);
	}

	// returns the index immediately following the match,
	// or 0 if not match was found
	public int match(String s, int fromIndex, MatchAction action) {
		throw new UnsupportedOperationException();  // TODO
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

		static abstract class SetElement {

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

		}

		static class CharacterClassSetElement extends SetElement {

			private final CharacterClass ccl;

			CharacterClassSetElement(CharacterClass ccl) {
				this.ccl = Check.notNull(ccl);
			}

			@Override
			public String toString() {
				return ccl.toString();
			}

		}

	}

	static abstract class CharacterClass {

		static class LiteralCharacterClass extends CharacterClass {

			private final char c;

			LiteralCharacterClass(char c) {
				this.c = c;
			}

			@Override
			public String toString() {
				return (isMagic(c) ? "%" : "") + Character.toString(c);
			}

		}

		static class SpecialCharacterClass extends CharacterClass {

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

			SpecialCharacterClass(ClassDesc desc, boolean complement) {
				this.desc = Check.notNull(desc);
				this.complement = complement;
			}

			@Override
			public String toString() {
				String s = desc.toString();
				return complement ? s.toUpperCase() : s;
			}

		}

		static class SetCharacterClass extends CharacterClass {

			private final CharacterSet cs;
			private final boolean complement;

			SetCharacterClass(CharacterSet cs, boolean complement) {
				this.cs = Check.notNull(cs);
				this.complement = complement;
			}

			@Override
			public String toString() {
				return (complement ? "[^" : "[") + cs.toString() + "]";
			}

		}

	}

	static abstract class PatternItem {

		static class EosPatternItem extends PatternItem {

			@Override
			public String toString() {
				return "$";
			}

		}

		static class CharacterClassPatternItem extends PatternItem {

			enum Modifier {

				EXACTLY_ONCE(""),  // no modifier
				LONGEST_ZERO_OR_MORE("*"),  // *
				SHORTEST_ZERO_OR_MORE("-"),  // -
				ONE_OR_MORE("+"),  // +
				AT_MOST_ONCE("?");  // ?

				private final String s;

				Modifier(String s) {
					this.s = s;
				}

				@Override
				public String toString() {
					return s;
				}

			}

			private final CharacterClass ccl;
			private final Modifier mod;

			CharacterClassPatternItem(CharacterClass ccl, Modifier mod) {
				this.ccl = Check.notNull(ccl);
				this.mod = Check.notNull(mod);
			}

			@Override
			public String toString() {
				return ccl.toString() + mod.toString();
			}

		}

		// %1, %2, ..., %9
		static class CaptureMatchPatternItem extends PatternItem {

			private final int index;

			CaptureMatchPatternItem(int index) {
				this.index = Check.inRange(index, 1, 9);
			}

			@Override
			public String toString() {
				return "%" + index;
			}

		}

		// %bxy
		static class BalancedPatternItem extends PatternItem {

			private final char first;
			private final char second;

			BalancedPatternItem(char first, char second) {
				this.first = first;
				this.second = second;
			}

			@Override
			public String toString() {
				return "%b" + first + second;
			}

		}

		// %f[set]
		static class FrontierPatternItem extends PatternItem {

			private final CharacterSet cs;

			FrontierPatternItem(CharacterSet cs) {
				this.cs = Check.notNull(cs);
			}

			@Override
			public String toString() {
				return "%f[" + cs.toString() + "]";
			}

		}

		// (pattern)
		static class CapturePatternItem extends PatternItem {

			private final List<PatternItem> subPattern;  // may be empty
			private final int index;

			CapturePatternItem(List<PatternItem> subPattern, int index) {
				this.subPattern = Check.notNull(subPattern);
				this.index = Check.positive(index);
			}

			@Override
			public String toString() {
				return "(" + patternItemsToString(subPattern) + ")";
			}

		}

	}

	static class PatternBuilder {

		private final CharSequence pattern;
		private int index;
		private int nextCaptureIndex;

		PatternBuilder(CharSequence pattern) {
			this.pattern = Check.notNull(pattern);
			this.index = 0;
			this.nextCaptureIndex = 1;
		}

		private char peek() {
			if (index < pattern.length()) {
				return pattern.charAt(index);
			}
			else {
				throw new IllegalArgumentException("unexpected end of string at character " + index);
			}
		}

		private String pretty(int idx) {
			return idx >= 0
					? idx < pattern.length()
							? "'" + pattern.charAt(idx) + "'"
							: "<eos>"
					: "<neg>";
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
				throw new IllegalArgumentException("error at character " + index + ": expected '"
						+ c + "', got " + pretty(index));
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

		private PatternItem.CharacterClassPatternItem.Modifier modifier() {
			if (!isEos()) {
				char d = peek();
				switch (d) {
					case '+': skip(1); return PatternItem.CharacterClassPatternItem.Modifier.ONE_OR_MORE;
					case '*': skip(1); return PatternItem.CharacterClassPatternItem.Modifier.LONGEST_ZERO_OR_MORE;
					case '-': skip(1); return PatternItem.CharacterClassPatternItem.Modifier.SHORTEST_ZERO_OR_MORE;
					case '?': skip(1); return PatternItem.CharacterClassPatternItem.Modifier.AT_MOST_ONCE;
				}
			}

			return PatternItem.CharacterClassPatternItem.Modifier.EXACTLY_ONCE;
		}

		private CharacterSet.SetElement characterSetElement() {
			CharacterClass ccl = maybeEscClass();
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
					return new CharacterSet.CharacterClassSetElement(litClass(c));
				}
			}
		}

		private CharacterSet characterSetBody() {
			List<CharacterSet.SetElement> elems = new ArrayList<>();
			while (!isEos() && peek() != ']') {
				elems.add(characterSetElement());
			}

			if (elems.isEmpty()) {
				throw new IllegalArgumentException("error at character " + index + ": empty character set");
			}

			return new CharacterSet(Collections.unmodifiableList(elems));
		}

		private PatternItem.FrontierPatternItem frontier() {
			consume("%f[");
			CharacterSet cs = characterSetBody();
			consume("]");
			return new PatternItem.FrontierPatternItem(cs);
		}

		private PatternItem.BalancedPatternItem balanced() {
			consume("%b");
			char x = next();
			char y = next();
			return new PatternItem.BalancedPatternItem(x, y);
		}

		private PatternItem.CaptureMatchPatternItem captureMatch() {
			consume("%");
			char c = next();
			if (c >= '0' && c <= '9') {
				int cidx = (int) c - (int) '0';
				return new PatternItem.CaptureMatchPatternItem(cidx);
			}
			else {
				throw new IllegalArgumentException("error at character " + index + ": expected '0'..'9', got "
						+ pretty(index));
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

		private CharacterClass.LiteralCharacterClass litClass(char c) {
			if (isMagic(c)) {
				throw new IllegalArgumentException("error at character " + index + ": unexpected magic character '" + c + "'");
			}
			return new CharacterClass.LiteralCharacterClass(c);
		}

		private static CharacterClass.SpecialCharacterClass.ClassDesc maybeClassDesc(int c) {
			switch (c) {
				case 'a': return CharacterClass.SpecialCharacterClass.ClassDesc.LETTERS;
				case 'c': return CharacterClass.SpecialCharacterClass.ClassDesc.CONTROL_CHARS;
				case 'd': return CharacterClass.SpecialCharacterClass.ClassDesc.DECIMAL_DIGITS;
				case 'g': return CharacterClass.SpecialCharacterClass.ClassDesc.PRINTABLE_EXCEPT_SPACE;
				case 'l': return CharacterClass.SpecialCharacterClass.ClassDesc.LOWERCASE_LETTERS;
				case 'p': return CharacterClass.SpecialCharacterClass.ClassDesc.PUNCTUATION;
				case 's': return CharacterClass.SpecialCharacterClass.ClassDesc.SPACE;
				case 'u': return CharacterClass.SpecialCharacterClass.ClassDesc.UPPERCASE_LETTERS;
				case 'w': return CharacterClass.SpecialCharacterClass.ClassDesc.ALPHANUMERIC;
				case 'x': return CharacterClass.SpecialCharacterClass.ClassDesc.HEXADECIMAL_DIGITS;
				default: return null;
			}
		}

		private CharacterClass maybeEscClass() {
			if (continuesWith("%")) {
				int o = charAtOffset(1);
				int lo = Character.toLowerCase(o);
				CharacterClass.SpecialCharacterClass.ClassDesc cd = maybeClassDesc(lo);
				if (cd != null) {
					consume("%");
					skip(1);
					return new CharacterClass.SpecialCharacterClass(cd, lo != o);
				}
				else {
					consume("%");
					char c = next();
					return new CharacterClass.LiteralCharacterClass(c);
				}
			}
			else {
				return null;
			}
		}

		private CharacterClass cclass() {

			if (continuesWith("[^")) {
				consume("[^");
				CharacterSet cs = characterSetBody();
				consume("]");
				return new CharacterClass.SetCharacterClass(cs, true);
			}

			if (continuesWith("[")) {
				consume("[");
				CharacterSet cs = characterSetBody();
				consume("]");
				return new CharacterClass.SetCharacterClass(cs, false);
			}


			CharacterClass ccl = maybeEscClass();

			if (ccl != null) {
				return ccl;
			}
			else {
				char c = next();
				if (c == '.') {
					return new CharacterClass.SpecialCharacterClass(CharacterClass.SpecialCharacterClass.ClassDesc.ALL, false);
				}
				else {
					return litClass(c);
				}
			}
		}

		private PatternItem.CharacterClassPatternItem characterClassPatternItem() {
			CharacterClass ccl = cclass();
			PatternItem.CharacterClassPatternItem.Modifier mod = modifier();
			return new PatternItem.CharacterClassPatternItem(ccl, mod);
		}

		private PatternItem patternItem() {
			if (continuesWith("(")) {
				return capture();
			}
			else if (continuesWith("%f[")) {
				return frontier();
			}
			else if (continuesWith("%b")) {
				return balanced();
			}
			else if (continuesWith("%") && charAtOffset(1) >= (int) '0' && charAtOffset(1) <= (int) '9') {
				return captureMatch();
			}
			else if (continuesWith("$") && charAtOffset(1) == -1) {
				skip(1);
				return new PatternItem.EosPatternItem();
			}
			else {
				return characterClassPatternItem();
			}
		}

		private PatternItem.CapturePatternItem capture() {
			consume('(');
			int capIdx = nextCaptureIndex++;
			List<PatternItem> items = new ArrayList<>();
			while (!isEos() && peek() != ')') {
				items.add(patternItem());
			}
			consume(')');
			return new PatternItem.CapturePatternItem(Collections.unmodifiableList(items), capIdx);
		}

		private List<PatternItem> parse() {
			List<PatternItem> items = new ArrayList<>();
			while (!isEos()) {
				items.add(patternItem());
			}
			return Collections.unmodifiableList(items);
		}

	}

	public static StringPattern fromString(String pattern, boolean ignoreCaret) {
		Check.notNull(pattern);

		final boolean anchoredBegin;
		final boolean anchoredEnd;

		if (pattern.startsWith("^")) {
			pattern = pattern.substring(1);
			anchoredBegin = !ignoreCaret;
		}
		else {
			anchoredBegin = false;
		}

		PatternBuilder builder = new PatternBuilder(pattern);
		List<PatternItem> items = builder.parse();

		return new StringPattern(items, anchoredBegin);
	}

	public static StringPattern fromString(String pattern) {
		return fromString(pattern, false);
	}

	private static String patternItemsToString(List<PatternItem> items) {
		StringBuilder builder = new StringBuilder();
		for (PatternItem pi : items) {
			builder.append(pi.toString());
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (anchoredBegin) builder.append('^');
		builder.append(patternItemsToString(items));
		return builder.toString();
	}

}

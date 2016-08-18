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

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.IllegalOperationAttemptException;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
import net.sandius.rembulan.core.ReturnVector;
import net.sandius.rembulan.core.impl.AbstractFunction0;
import net.sandius.rembulan.core.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.StringLib;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultStringLib extends StringLib {

	private final Function _gsub;
	private final Function _pack;
	private final Function _packsize;
	private final Function _unpack;

	public DefaultStringLib() {
		this._gsub = new UnimplementedFunction("string.gsub");  // TODO
		this._pack = new UnimplementedFunction("string.pack");  // TODO
		this._packsize = new UnimplementedFunction("string.packsize");  // TODO
		this._unpack = new UnimplementedFunction("string.unpack");  // TODO
	}

	@Override
	public Function _byte() {
		return Byte.INSTANCE;
	}

	@Override
	public Function _char() {
		return Char.INSTANCE;
	}

	@Override
	public Function _dump() {
		return Dump.INSTANCE;
	}

	@Override
	public Function _find() {
		return Find.INSTANCE;
	}

	@Override
	public Function _format() {
		return Format.INSTANCE;
	}

	@Override
	public Function _gmatch() {
		return GMatch.INSTANCE;
	}

	@Override
	public Function _gsub() {
		return _gsub;
	}

	@Override
	public Function _len() {
		return Len.INSTANCE;
	}

	@Override
	public Function _lower() {
		return Lower.INSTANCE;
	}

	@Override
	public Function _match() {
		return Match.INSTANCE;
	}

	@Override
	public Function _pack() {
		return _pack;
	}

	@Override
	public Function _packsize() {
		return _packsize;
	}

	@Override
	public Function _rep() {
		return Rep.INSTANCE;
	}

	@Override
	public Function _reverse() {
		return Reverse.INSTANCE;
	}

	@Override
	public Function _sub() {
		return Sub.INSTANCE;
	}

	@Override
	public Function _unpack() {
		return _unpack;
	}

	@Override
	public Function _upper() {
		return Upper.INSTANCE;
	}

	public static class Pattern {

		private Pattern() {
		}

		public static Pattern fromString(String pattern, boolean ignoreCaret) {
			throw new UnsupportedOperationException();  // TODO
		}

		public static Pattern fromString(String pattern) {
			return fromString(pattern, false);
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

	}

	private static int lowerBound(int i, int len) {
		int j = i < 0 ? len + i + 1 : i;
		return j < 1 ? 1 : j;
	}

	private static int upperBound(int i, int len) {
		int j = i < 0 ? len + i + 1 : i;
		return j > len ? len : j;
	}

	public static class Byte extends AbstractLibFunction {

		public static final Byte INSTANCE = new Byte();

		@Override
		protected String name() {
			return "byte";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			String s = args.nextString();
			int i = args.optNextInt(1);
			int j = args.optNextInt(i);

			int len = s.length();

			i = lowerBound(i, len);
			j = upperBound(j, len);

			context.getReturnVector().reset();

			for (int idx = i; idx <= j; idx++) {
				// FIXME: these are not bytes!
				char c = s.charAt(idx - 1);
				context.getReturnVector().push((long) c);
			}
		}

	}

	public static class Char extends AbstractLibFunction {

		public static final Char INSTANCE = new Char();

		@Override
		protected String name() {
			return "char";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			char[] chars = new char[args.size()];

			for (int i = 0; i < chars.length; i++) {
				chars[i] = (char) args.nextIntRange("value", 0, 255);
			}

			String s = String.valueOf(chars);
			context.getReturnVector().setTo(s);
		}

	}

	public static class Dump extends AbstractLibFunction {

		public static final Dump INSTANCE = new Dump();

		@Override
		protected String name() {
			return "dump";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Function f = args.nextFunction();
			boolean strip = args.optNextBoolean(false);

			throw new IllegalOperationAttemptException("unable to dump given function");
		}

	}

	public static class Find extends AbstractLibFunction {

		public static final Find INSTANCE = new Find();

		@Override
		protected String name() {
			return "find";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			String s = args.nextString();
			String pattern = args.nextString();
			int init = args.optNextInt(1);
			boolean plain = args.optNextBoolean(false);

			init = lowerBound(init, s.length());

			if (plain) {
				// find a substring
				int at = s.indexOf(pattern, init - 1);
				if (at >= 0) {
					context.getReturnVector().setTo(at + 1, at + pattern.length());
				}
				else {
					context.getReturnVector().setTo(null);
				}
			}
			else {
				// find a pattern
				Pattern pat = Pattern.fromString(pattern);

				final ArrayList<Object> results = new ArrayList<>();
				final ArrayList<Object> captures = new ArrayList<>();

				int nextIndex = pat.match(s, init, new Pattern.MatchAction() {

					@Override
					public void onMatch(String s, int firstIndex, int lastIndex) {
						results.add((long) firstIndex);
						results.add((long) lastIndex);
					}

					@Override
					public void onCapture(String s, int index, String value) {
						captures.add(value != null ? value : (long) index);
					}
				});

				if (nextIndex < 1) {
					// pattern not found
					context.getReturnVector().setTo(null);
				}
				else {
					// pattern found
					ReturnVector returnVector = context.getReturnVector();
					returnVector.reset();
					for (Object r : results) {
						returnVector.push(r);
					}
					for (Object c : captures) {
						returnVector.push(c);
					}
				}
			}
		}

	}

	public static class Format extends AbstractLibFunction {

		public static final Format INSTANCE = new Format();

		@Override
		protected String name() {
			return "format";
		}

		private static String optionToString(char c) {
			if (Character.isLetterOrDigit(c)) {
				return "%" + c;
			}
			else {
				return "%<\\" + ((int) c) + ">";
			}
		}

		private static void repeatChar(char c, int num, StringBuilder bld) {
			for (int i = 0; i < num; i++) {
				bld.append(c);
			}
		}

		private static String padLeft(String s, char c, int width) {
			int diff = width - s.length();

			if (diff > 0) {
				StringBuilder bld = new StringBuilder();
				repeatChar(c, diff, bld);
				bld.append(s);
				return bld.toString();
			}
			else {
				return s;
			}
		}

		private static String padRight(String s, char c, int width) {
			int diff = width - s.length();

			if (diff > 0) {
				StringBuilder bld = new StringBuilder();
				bld.append(s);
				repeatChar(c, diff, bld);
				return bld.toString();
			}
			else {
				return s;
			}
		}

		private static final long L_1E18  = 1000000000000000000L;
		private static final long L_9E18  =  9 * L_1E18;
		private static final long L_10E18 = 10 * L_1E18;  // overflows, and that's the point

		public static String longToUnsignedString(long x) {

			// Maximum value representable by signed long is    (2^63 - 1)
			//                             by unsigned long is  (2^64 - 1)
			//
			// Now,
			//        9e18 < (2^63 - 1) < 10e18 < (2^64 - 1) < 20e18
			//
			// If signed(x) >= 0, then signed(x) == unsigned(x).
			// If signed(x) < 0, then unsigned(x) >= 2^63, and therefore unsigned(x) > unsigned(9e18).
			// Now we only need to check whether unsigned(x) >= unsigned(10e18) -- if so,
			// the leftmost digit is necessarily '1' (since 20e18 > 2^64), followed by 19 digits;
			// otherwise, the leftmost digit is '9', followed by 18 digits.
			// In 2's complement, for a, b such that both unsigned(a) >= 2^63 and unsigned(b) >= 2^63,
			// (signed(a) < signed(b)) iff (unsigned(a) < unsigned(b)),
			// so the test is equivalent to signed(x) >= signed(10e18).

			return x >= 0
					? Long.toString(x)
					: (x >= L_10E18
							? '1' + padLeft(Long.toString(x - L_10E18), '0', 19)
							: '9' + padLeft(Long.toString(x - L_9E18), '0', 18));
		}

		private int literal(String fmt, int from, StringBuilder bld) {
			int index = from;
			while (index < fmt.length()) {
				char c = fmt.charAt(index++);

				if (c != '%') {
					bld.append(c);
				}
				else {
					if (index < fmt.length() && fmt.charAt(index) == '%') {
						// literal '%'
						bld.append('%');
						index += 1;
					}
					else {
						return index;
					}
				}

			}
			return -1;
		}

		private IllegalArgumentException invalidOptionException(char c) {
			return new IllegalArgumentException("invalid option '" + optionToString(c) + "' to 'format'");
		}

		private int setFlag(int flags, int mask) {
			if ((flags & mask) != 0) {
				throw new IllegalArgumentException("illegal format (repeated flags)");
			}
			return flags | mask;
		}

		private boolean hasFlag(int flags, int mask) {
			return (flags & mask) != 0;
		}

		private String sign(boolean nonNegative, int flags) {
			return nonNegative
					? (hasFlag(flags, FLAG_SIGN_ALWAYS)
							? "+"
							: (hasFlag(flags, FLAG_ZERO_PAD)
									? " "
									: ""))
					: "-";
		}

		private String altForm(long value, int flags, String prefix) {
			return value != 0 && hasFlag(flags, FLAG_ALT_FORM) ? prefix : "";
		}

		private String padded(int precision, String digits) {
			return precision >= 0
					? padLeft("0".equals(digits) ? "" : digits, '0', precision)
					: digits;
		}

		private String trimmed(int precision, String chars) {
			return precision >= 0
					? chars.substring(0, Math.min(chars.length(), precision))
					: chars;
		}

		private String justified(int width, int flags, String digits) {
			return width >= 0
					? (hasFlag(flags, FLAG_LEFTJUSTIFY)
							? padRight(digits, ' ', width)
							: padLeft(digits, ' ', width))
					: digits;
		}

		private static final int FLAG_LEFTJUSTIFY = 1 << 1;
		private static final int FLAG_SIGN_ALWAYS = 1 << 2;
		private static final int FLAG_SIGN_SPACE = 1 << 3;
		private static final int FLAG_ZERO_PAD = 1 << 4;
		private static final int FLAG_ALT_FORM = 1 << 5;

		private int placeholder(String fmt, int from, StringBuilder bld, ArgumentIterator args) {
			if (!args.hasNext()) {
				throw new BadArgumentException(args.size() + 1, name(), "no value");
			}

			int index = from;

			char c;

			int flags = 0;

			// flags
			{
				boolean wasFlag = true;

				do {
					if (index < fmt.length()) {
						c = fmt.charAt(index++);
					}
					else {
						throw invalidOptionException('\0');
					}

					switch (c) {
						case '-': flags = setFlag(flags, FLAG_LEFTJUSTIFY); break;
						case '+': flags = setFlag(flags, FLAG_SIGN_ALWAYS); break;
						case ' ': flags = setFlag(flags, FLAG_SIGN_SPACE); break;
						case '0': flags = setFlag(flags, FLAG_ZERO_PAD); break;
						case '#': flags = setFlag(flags, FLAG_ALT_FORM); break;

						default:
							// not a flag, take the character back
							index -= 1;
							wasFlag = false;
							break;
					}

				} while (wasFlag);
			}

			// width
			int width = -1;

			{
				boolean wasWidth = true;

				do {
					if (index < fmt.length()) {
						c = fmt.charAt(index++);
					}
					else {
						throw invalidOptionException('\0');
					}

					if (c >= '0' && c <= '9') {
						width = Math.max(0, width) * 10 + (c - '0');
						if (width >= 100) {
							throw new IllegalArgumentException("illegal format (width or precision too long)");
						}
					}
					else {
						// not a width specifier, put back
						index -= 1;
						wasWidth = false;
					}

				} while (wasWidth);
			}

			// precision
			int precision = -1;

			{
				if (index < fmt.length() && fmt.charAt(index) == '.') {
					index += 1;  // skip the '.'
					precision = 0;

					boolean wasPrecision = true;
					do {
						if (index < fmt.length()) {
							c = fmt.charAt(index++);
						}
						else {
							throw invalidOptionException('\0');
						}

						if (c >= '0' && c <= '9') {
							precision = precision * 10 + (c - '0');
							if (precision >= 100) {
								throw new IllegalArgumentException("illegal format (width or precision too long)");
							}
						}
						else {
							// not a width specifier, put back
							index -= 1;
							wasPrecision = false;
						}

					} while (wasPrecision);

				}
			}

			// type
			{
				char d = fmt.charAt(index++);

				final String result;

				switch (d) {
					case 'd':
					case 'i': {
						long l = args.nextInteger();

						String ls = LuaFormat.toString(l);
						String digits = l < 0 ? ls.substring(1) : ls;  // ignore the sign, we'll re-attach it later
						result = justified(width, flags,
								sign(l >= 0, flags) + padded(precision, digits));
						break;
					}

					case 'u': {
						long l = args.nextInteger();

						String digits = longToUnsignedString(l);
						result = justified(width, flags,
								padded(precision, digits));
						break;
					}

					case 'o': {
						long l = args.nextInteger();

						String digits = Long.toOctalString(l);
						result = justified(width, flags,
								altForm(l, flags, "0") + padded(precision, digits));
						break;
					}

					case 'x':
					case 'X': {
						long l = args.nextInteger();

						String digits = Long.toHexString(l);
						String lowerCaseResult = justified(width, flags,
								altForm(l, flags, "0x") + padded(precision, digits));

						result = d == 'X' ? lowerCaseResult.toUpperCase() : lowerCaseResult;
						break;
					}

					case 'c':
						result = justified(width, flags, Character.toString((char) args.nextInteger()));
						break;

					case 'f':
					case 'a':
					case 'A':
					case 'e':
					case 'E':
					case 'g':
					case 'G': {
						double v = args.nextFloat();

						if (Double.isNaN(v) || Double.isInfinite(v)) {
							final String chars;

							chars = Double.isNaN(v)
									? LuaFormat.NAN
									: sign(v > 0, flags) + LuaFormat.POS_INF;

							result = justified(width, flags, chars);
						}
						else {
							StringBuilder fmtBld = new StringBuilder();
							fmtBld.append('%');
							if (hasFlag(flags, FLAG_LEFTJUSTIFY)) fmtBld.append('-');
							if (hasFlag(flags, FLAG_SIGN_ALWAYS)) fmtBld.append('+');
							if (hasFlag(flags, FLAG_SIGN_SPACE)) fmtBld.append(' ');
							if (hasFlag(flags, FLAG_ZERO_PAD)) fmtBld.append('0');
							if (hasFlag(flags, FLAG_ALT_FORM)) fmtBld.append('#');

							if (width > 0) fmtBld.append(width);
							// width required by Formatter, but not supplied
							else if (hasFlag(flags, FLAG_ZERO_PAD)) fmtBld.append('1');

							if (precision > 0) fmtBld.append('.').append(precision);
							fmtBld.append(d);
							String formatted = String.format(fmtBld.toString(), v);

							if (d == 'a' || d == 'A') {
								// insert the '+' sign to the exponent
								int p = formatted.indexOf(d == 'a' ? 'p' : 'P') + 1;
								if (formatted.charAt(p) != '-') {
									formatted = formatted.substring(0, p) + '+' + formatted.substring(p);
								}
							}

							result = formatted;
						}

						break;
					}

					case 's': {
						Object v = args.nextAny();
						String s = Conversions.stringValueOf(v);
						if (s != null) {
							result = justified(width, flags, trimmed(precision, s));
						}
						else {
							// TODO: use __tostring for non-string arguments
							throw new UnsupportedOperationException("not implemented: tostring");
						}
						break;
					}

					case 'q': {
						String s = args.nextString();
						result = LuaFormat.escape(s);
						break;
					}

					default:
						throw new IllegalArgumentException("invalid option '" + optionToString(d) + "' to 'format'");

				}

				bld.append(result);
			}

			return index < fmt.length() ? index : -1;
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			String fmt = args.nextString();

			StringBuilder bld = new StringBuilder();

			int idx = 0;
			do {
				idx = literal(fmt, idx, bld);
				if (idx >= 0) {
					idx = placeholder(fmt, idx, bld, args);
				}
			} while (idx >= 0);

			context.getReturnVector().setTo(bld.toString());
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			// TODO: needed for tostring
			super.resume(context, suspendedState);
		}

	}

	public static class GMatch extends AbstractLibFunction {

		public static final GMatch INSTANCE = new GMatch();

		public static class IteratorFunction extends AbstractFunction0 {

			public final String string;
			public final Pattern pattern;
			private final AtomicInteger index;

			public IteratorFunction(String string, Pattern pattern) {
				this.string = Check.notNull(string);
				this.pattern = Check.notNull(pattern);
				this.index = new AtomicInteger(1);
			}

			@Override
			public void invoke(ExecutionContext context) throws ControlThrowable {
				final String[] fullMatch = new String[] { null };
				final ArrayList<Object> captures = new ArrayList<>();

				int oldIndex;
				int nextIndex;

				do {
					oldIndex = index.get();

					fullMatch[0] = null;
					captures.clear();

					nextIndex = pattern.match(string, oldIndex, new Pattern.MatchAction() {
						@Override
						public void onMatch(String s, int firstIndex, int lastIndex) {
							fullMatch[0] = s.substring(firstIndex - 1, lastIndex);
						}

						@Override
						public void onCapture(String s, int index, String value) {
							captures.add(value != null ? value : (long) index);
						}
					});

				} while (!index.compareAndSet(oldIndex, nextIndex));

				if (nextIndex < 1) {
					// no match
					context.getReturnVector().reset();
				}
				else {
					// match
					if (captures.isEmpty()) {
						context.getReturnVector().setTo(fullMatch);
					}
					else {
						context.getReturnVector().reset();
						for (Object c : captures) {
							context.getReturnVector().push(c);
						}
					}
				}
			}

			@Override
			public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
				throw new NonsuspendableFunctionException(this.getClass());
			}
		}

		@Override
		protected String name() {
			return "gmatch";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			String s = args.nextString();
			String pattern = args.nextString();

			Pattern pat = Pattern.fromString(pattern, true);

			Function f = new IteratorFunction(s, pat);

			context.getReturnVector().setTo(f);
		}

	}

	public static class Len extends AbstractLibFunction {

		public static final Len INSTANCE = new Len();

		@Override
		protected String name() {
			return "len";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			String s = args.nextString();
			context.getReturnVector().setTo((long) s.length());
		}

	}

	public static class Lower extends AbstractLibFunction {

		public static final Lower INSTANCE = new Lower();

		@Override
		protected String name() {
			return "lower";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			String s = args.nextString();
			context.getReturnVector().setTo(s.toLowerCase());
		}

	}

	public static class Match extends AbstractLibFunction {

		public static final Match INSTANCE = new Match();

		@Override
		protected String name() {
			return "match";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			String s = args.nextString();
			String pattern = args.nextString();
			int init = args.optNextInt(1);

			init = lowerBound(init, s.length());

			Pattern pat = Pattern.fromString(pattern);

			final String[] fullMatch = new String[] { null };
			final ArrayList<Object> captures = new ArrayList<>();

			int nextIndex = pat.match(s, init, new Pattern.MatchAction() {
				@Override
				public void onMatch(String s, int firstIndex, int lastIndex) {
					fullMatch[0] = s.substring(firstIndex - 1, lastIndex);
				}

				@Override
				public void onCapture(String s, int index, String value) {
					captures.add(value != null ? value : (long) index);
				}
			});

			if (nextIndex < 1) {
				// no match found
				context.getReturnVector().setTo(null);
			}
			else {
				// match was found
				if (captures.isEmpty()) {
					// no captures
					context.getReturnVector().setTo(fullMatch[0]);
				}
				else {
					context.getReturnVector().reset();
					for (Object c : captures) {
						context.getReturnVector().push(c);
					}
				}
			}
		}

	}

	public static class Rep extends AbstractLibFunction {

		public static final Rep INSTANCE = new Rep();

		@Override
		protected String name() {
			return "rep";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			String s = args.nextString();
			int n = args.nextInt();
			String sep = args.optNextString("");

			final String result;
			if (n > 0) {
				StringBuilder bld = new StringBuilder();

				for (int i = 0; i < n; i++) {
					bld.append(s);
					if (i + 1 < n) {
						bld.append(sep);
					}
				}

				result = bld.toString();
			}
			else {
				result = "";
			}

			context.getReturnVector().setTo(result);
		}

	}

	public static class Reverse extends AbstractLibFunction {

		public static final Reverse INSTANCE = new Reverse();

		@Override
		protected String name() {
			return "reverse";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			String s = args.nextString();

			int len = s.length();
			char[] chars = new char[len];

			for (int i = 0; i < chars.length; i++) {
				chars[i] = s.charAt(len - 1 - i);
			}

			String result = String.valueOf(chars);

			context.getReturnVector().setTo(result);
		}

	}

	public static class Sub extends AbstractLibFunction {

		public static final Sub INSTANCE = new Sub();

		@Override
		protected String name() {
			return "sub";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			String s = args.nextString();
			int i = args.nextInt();
			int j = args.optNextInt(-1);

			int len = s.length();
			i = lowerBound(i, len) - 1;
			j = upperBound(j, len);

			String result = s.substring(i, j);

			context.getReturnVector().setTo(result);
		}

	}

	public static class Upper extends AbstractLibFunction {

		public static final Upper INSTANCE = new Upper();

		@Override
		protected String name() {
			return "upper";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			String s = args.nextString();
			context.getReturnVector().setTo(s.toUpperCase());
		}

	}

}

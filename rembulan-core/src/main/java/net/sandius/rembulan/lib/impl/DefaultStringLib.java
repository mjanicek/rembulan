package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.IllegalOperationAttemptException;
import net.sandius.rembulan.core.LInteger;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.Preemption;
import net.sandius.rembulan.core.impl.Function0;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.StringLib;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultStringLib extends StringLib {

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
		return null;  // TODO
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
		return null;  // TODO
	}

	@Override
	public Function _packsize() {
		return null;  // TODO
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
		return null;  // TODO
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

	public static class Byte extends LibFunction {

		public static final Byte INSTANCE = new Byte();

		@Override
		protected String name() {
			return "byte";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			String s = args.nextString();
			int i = args.optNextInt(1);
			int j = args.optNextInt(i);

			int len = s.length();

			i = lowerBound(i, len);
			j = upperBound(j, len);

			context.getObjectSink().reset();

			for (int idx = i; idx <= j; idx++) {
				// FIXME: these are not bytes!
				char c = s.charAt(idx - 1);
				context.getObjectSink().push(LInteger.valueOf(c));
			}

			return null;
		}

	}

	public static class Char extends LibFunction {

		public static final Char INSTANCE = new Char();

		@Override
		protected String name() {
			return "char";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			char[] chars = new char[args.size()];

			for (int i = 0; i < chars.length; i++) {
				chars[i] = (char) args.nextIntRange("value", 0, 255);
			}

			String s = String.valueOf(chars);
			context.getObjectSink().setTo(s);
			return null;
		}

	}

	public static class Dump extends LibFunction {

		public static final Dump INSTANCE = new Dump();

		@Override
		protected String name() {
			return "dump";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Function f = args.nextFunction();
			boolean strip = Conversions.objectToBoolean(args.optNextAny());

			throw new IllegalOperationAttemptException("unable to dump given function");
		}

	}

	public static class Find extends LibFunction {

		public static final Find INSTANCE = new Find();

		@Override
		protected String name() {
			return "find";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			String s = args.nextString();
			String pattern = args.nextString();
			int init = args.optNextInt(1);
			boolean plain = args.optNextBoolean(false);

			init = lowerBound(init, s.length());

			if (plain) {
				// find a substring
				int at = s.indexOf(pattern, init - 1);
				if (at >= 0) {
					context.getObjectSink().setTo(at + 1, at + pattern.length());
					return null;
				}
				else {
					context.getObjectSink().setTo(null);
					return null;
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
					context.getObjectSink().setTo(null);
					return null;
				}
				else {
					// pattern found
					ObjectSink objectSink = context.getObjectSink();
					objectSink.reset();
					for (Object r : results) {
						objectSink.push(r);
					}
					for (Object c : captures) {
						objectSink.push(c);
					}
					return null;
				}
			}
		}

	}

	public static class Format extends LibFunction {

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

		private int placeholder(String fmt, int from, StringBuilder bld, CallArguments args) {
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
						double v = args.nextNumber().doubleValue();

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
						String s = Conversions.objectAsString(v);
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
						throw new UnsupportedOperationException("not implemented: %q");  // TODO
					}

					default:
						throw new IllegalArgumentException("invalid option '" + optionToString(d) + "' to 'format'");

				}

				bld.append(result);
			}

			return index < fmt.length() ? index : -1;
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			String fmt = args.nextString();

			StringBuilder bld = new StringBuilder();

			int idx = 0;
			do {
				idx = literal(fmt, idx, bld);
				if (idx >= 0) {
					idx = placeholder(fmt, idx, bld, args);
				}
			} while (idx >= 0);

			context.getObjectSink().setTo(bld.toString());
			return null;
		}

		@Override
		protected Preemption _resume(ExecutionContext context, Object suspendedState) {
			// TODO: needed for tostring
			return super._resume(context, suspendedState);
		}

	}

	public static class GMatch extends LibFunction {

		public static final GMatch INSTANCE = new GMatch();

		public static class IteratorFunction extends Function0 {

			public final String string;
			public final Pattern pattern;
			private final AtomicInteger index;

			public IteratorFunction(String string, Pattern pattern) {
				this.string = Check.notNull(string);
				this.pattern = Check.notNull(pattern);
				this.index = new AtomicInteger(1);
			}

			@Override
			public Preemption invoke(ExecutionContext context) {
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
					context.getObjectSink().reset();
					return null;
				}
				else {
					// match
					if (captures.isEmpty()) {
						context.getObjectSink().setTo(fullMatch);
					}
					else {
						context.getObjectSink().reset();
						for (Object c : captures) {
							context.getObjectSink().push(c);
						}
					}
					return null;
				}
			}

			@Override
			public Preemption resume(ExecutionContext context, Object suspendedState) {
				throw new NonsuspendableFunctionException(this.getClass());
			}
		}

		@Override
		protected String name() {
			return "gmatch";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			String s = args.nextString();
			String pattern = args.nextString();

			Pattern pat = Pattern.fromString(pattern, true);

			Function f = new IteratorFunction(s, pat);

			context.getObjectSink().setTo(f);
			return null;
		}

	}

	public static class Len extends LibFunction {

		public static final Len INSTANCE = new Len();

		@Override
		protected String name() {
			return "len";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			String s = args.nextString();
			context.getObjectSink().setTo(LInteger.valueOf(s.length()));
			return null;
		}

	}

	public static class Lower extends LibFunction {

		public static final Lower INSTANCE = new Lower();

		@Override
		protected String name() {
			return "lower";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			String s = args.nextString();
			context.getObjectSink().setTo(s.toLowerCase());
			return null;
		}

	}

	public static class Match extends LibFunction {

		public static final Match INSTANCE = new Match();

		@Override
		protected String name() {
			return "match";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
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
				context.getObjectSink().setTo(null);
				return null;
			}
			else {
				// match was found
				if (captures.isEmpty()) {
					// no captures
					context.getObjectSink().setTo(fullMatch[0]);
				}
				else {
					context.getObjectSink().reset();
					for (Object c : captures) {
						context.getObjectSink().push(c);
					}
				}
				return null;
			}
		}

	}

	public static class Rep extends LibFunction {

		public static final Rep INSTANCE = new Rep();

		@Override
		protected String name() {
			return "rep";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
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

			context.getObjectSink().setTo(result);
			return null;
		}

	}

	public static class Reverse extends LibFunction {

		public static final Reverse INSTANCE = new Reverse();

		@Override
		protected String name() {
			return "reverse";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			String s = args.nextString();

			int len = s.length();
			char[] chars = new char[len];

			for (int i = 0; i < chars.length; i++) {
				chars[i] = s.charAt(len - 1 - i);
			}

			String result = String.valueOf(chars);

			context.getObjectSink().setTo(result);
			return null;
		}

	}

	public static class Sub extends LibFunction {

		public static final Sub INSTANCE = new Sub();

		@Override
		protected String name() {
			return "sub";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			String s = args.nextString();
			int i = args.nextInt();
			int j = args.optNextInt(-1);

			int len = s.length();
			i = lowerBound(i, len) - 1;
			j = upperBound(j, len);

			String result = s.substring(i, j);

			context.getObjectSink().setTo(result);
			return null;
		}

	}

	public static class Upper extends LibFunction {

		public static final Upper INSTANCE = new Upper();

		@Override
		protected String name() {
			return "upper";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			String s = args.nextString();
			context.getObjectSink().setTo(s.toUpperCase());
			return null;
		}

	}

}

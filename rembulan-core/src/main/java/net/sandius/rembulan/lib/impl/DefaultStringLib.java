package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.IllegalOperationAttemptException;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.impl.Function0;
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
		protected void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable {
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
				context.getObjectSink().push((long) c);
			}
		}

	}

	public static class Char extends LibFunction {

		public static final Char INSTANCE = new Char();

		@Override
		protected String name() {
			return "char";
		}

		@Override
		protected void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable {
			char[] chars = new char[args.size()];

			for (int i = 0; i < chars.length; i++) {
				chars[i] = (char) args.nextIntRange("value", 0, 255);
			}

			String s = String.valueOf(chars);
			context.getObjectSink().setTo(s);
		}

	}

	public static class Dump extends LibFunction {

		public static final Dump INSTANCE = new Dump();

		@Override
		protected String name() {
			return "dump";
		}

		@Override
		protected void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable {
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
		protected void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable {
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
				}
				else {
					context.getObjectSink().setTo(null);
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

		private static String padLeft(String s, char c, int width) {
			int diff = width - s.length();

			if (diff > 0) {
				StringBuilder bld = new StringBuilder();
				for (int i = 0; i < diff; i++) {
					bld.append(c);
				}
				bld.append(s);
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

		@Override
		protected void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable {
			String fmt = args.nextString();

			StringBuilder bld = new StringBuilder();

			int index = 0;
			while (index < fmt.length()) {
				final char c = fmt.charAt(index);

				switch (c) {
					case '%': {
						index++;

						final char d = index < fmt.length() ? fmt.charAt(index) : '\0';
						switch (d) {

							case '%':
								bld.append('%');
								break;

							case 'd':
							case 'i':
								bld.append(args.nextInteger());
								break;

							case 'u':
								bld.append(longToUnsignedString(args.nextInteger()));
								break;

							case 'o':
								bld.append(Long.toOctalString(args.nextInteger()));
								break;

							case 'x':
							case 'X': {
								String hex = Long.toHexString(args.nextInteger());
								bld.append(d == 'X' ? hex.toUpperCase() : hex);
								break;
							}

							case 'c':
								bld.append((char) args.nextInteger());
								break;

							case 's': {
								Object v = args.nextAny();
								String s = Conversions.objectAsString(v);
								if (s != null) {
									bld.append(s);
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

						break;
					}

					default:
						bld.append(c);
						break;
				}

				index++;
			}

			String result = bld.toString();

			context.getObjectSink().setTo(result);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			// TODO: needed for tostring
			super.resume(context, suspendedState);
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
					context.getObjectSink().reset();
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
		protected void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable {
			String s = args.nextString();
			String pattern = args.nextString();

			Pattern pat = Pattern.fromString(pattern, true);

			Function f = new IteratorFunction(s, pat);

			context.getObjectSink().setTo(f);
		}

	}

	public static class Len extends LibFunction {

		public static final Len INSTANCE = new Len();

		@Override
		protected String name() {
			return "len";
		}

		@Override
		protected void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable {
			String s = args.nextString();
			context.getObjectSink().setTo((long) s.length());
		}

	}

	public static class Lower extends LibFunction {

		public static final Lower INSTANCE = new Lower();

		@Override
		protected String name() {
			return "lower";
		}

		@Override
		protected void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable {
			String s = args.nextString();
			context.getObjectSink().setTo(s.toLowerCase());
		}

	}

	public static class Match extends LibFunction {

		public static final Match INSTANCE = new Match();

		@Override
		protected String name() {
			return "match";
		}

		@Override
		protected void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable {
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
		protected void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable {
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
		}

	}

	public static class Reverse extends LibFunction {

		public static final Reverse INSTANCE = new Reverse();

		@Override
		protected String name() {
			return "reverse";
		}

		@Override
		protected void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable {
			String s = args.nextString();

			int len = s.length();
			char[] chars = new char[len];

			for (int i = 0; i < chars.length; i++) {
				chars[i] = s.charAt(len - 1 - i);
			}

			String result = String.valueOf(chars);

			context.getObjectSink().setTo(result);
		}

	}

	public static class Sub extends LibFunction {

		public static final Sub INSTANCE = new Sub();

		@Override
		protected String name() {
			return "sub";
		}

		@Override
		protected void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable {
			String s = args.nextString();
			int i = args.nextInt();
			int j = args.optNextInt(-1);

			int len = s.length();
			i = lowerBound(i, len) - 1;
			j = upperBound(j, len);

			String result = s.substring(i, j);

			context.getObjectSink().setTo(result);
		}

	}

	public static class Upper extends LibFunction {

		public static final Upper INSTANCE = new Upper();

		@Override
		protected String name() {
			return "upper";
		}

		@Override
		protected void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable {
			String s = args.nextString();
			context.getObjectSink().setTo(s.toUpperCase());
		}

	}

}

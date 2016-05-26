package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.RawOperators;
import net.sandius.rembulan.lib.StringLib;

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
		return null;  // TODO
	}

	@Override
	public Function _find() {
		return null;  // TODO
	}

	@Override
	public Function _format() {
		return null;  // TODO
	}

	@Override
	public Function _gmatch() {
		return null;  // TODO
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
		return null;  // TODO
	}

	@Override
	public Function _match() {
		return null;  // TODO
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
		return null;  // TODO
	}

	@Override
	public Function _reverse() {
		return null;  // TODO
	}

	@Override
	public Function _sub() {
		return null;  // TODO
	}

	@Override
	public Function _unpack() {
		return null;  // TODO
	}

	@Override
	public Function _upper() {
		return null;  // TODO
	}

	private static int correctIndex(int i, int len) {
		if (i < 0) {
			i = len - i;
		}

		if (i < 1) {
			i = 1;
		}

		if (i > len) {
			i = len;
		}

		return i;
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

			int len = RawOperators.stringLen(s);

			// correct indices
			i = correctIndex(i, len);
			j = correctIndex(j, len);

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
				chars[i] = (char) args.nextInt();
			}

			String s = String.valueOf(chars);
			context.getObjectSink().setTo(s);
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

}

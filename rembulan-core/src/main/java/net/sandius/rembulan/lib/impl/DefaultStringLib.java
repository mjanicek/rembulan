package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
import net.sandius.rembulan.core.RawOperators;
import net.sandius.rembulan.core.impl.FunctionAnyarg;
import net.sandius.rembulan.lib.LibUtils;
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

	public static class Byte extends FunctionAnyarg {

		public static final Byte INSTANCE = new Byte();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			String s = LibUtils.checkString("byte", args, 0);
			int i = args.length >= 1 ? LibUtils.checkInt("byte", args, 1) : 1;
			int j = args.length >= 2 ? LibUtils.checkInt("byte", args, 2) : i;

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

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Char extends FunctionAnyarg {

		public static final Char INSTANCE = new Char();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			char[] chars = new char[args.length];

			for (int i = 0; i < args.length; i++) {
				chars[i] = (char) LibUtils.checkInt("char", args, i);
			}

			String s = String.valueOf(chars);
			context.getObjectSink().setTo(s);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Len extends FunctionAnyarg {

		public static final Len INSTANCE = new Len();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			String s = LibUtils.checkString("len", args, 0);
			context.getObjectSink().setTo((long) s.length());
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

}

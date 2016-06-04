package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Coroutine;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.ValueTypeNamer;
import net.sandius.rembulan.core.impl.FunctionAnyarg;
import net.sandius.rembulan.core.impl.Varargs;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.LibUtils;
import net.sandius.rembulan.lib.UnexpectedArgumentException;
import net.sandius.rembulan.util.Check;

import java.util.NoSuchElementException;

import static net.sandius.rembulan.core.PlainValueTypeNamer.*;

public abstract class LibFunction extends FunctionAnyarg {

	public static class CallArguments {

		private final ValueTypeNamer namer;

		private final String name;
		private final Object[] args;
		private int index;

		private CallArguments(ValueTypeNamer namer, String name, Object[] args, int index) {
			this.namer = Check.notNull(namer);
			this.name = Check.notNull(name);
			this.args = Check.notNull(args);
			this.index = Check.nonNegative(index);
		}

		public CallArguments(ValueTypeNamer namer, String name, Object[] args) {
			this(namer, name, args, 0);
		}

		public ValueTypeNamer namer() {
			return namer;
		}

		public void rewind() {
			index = 0;
		}

		public void skip() {
			index += 1;
		}

		public boolean hasNext() {
			return index < args.length;
		}

		public int size() {
			return args.length;
		}

		public int tailSize() {
			return Math.max(args.length - index, 0);
		}

		public Object[] getAll() {
			return args;
		}

		public Object[] getTail() {
			return Varargs.from(args, index);
		}

		protected BadArgumentException badArgument(Throwable cause) {
			return new BadArgumentException(index + 1, name, cause);
		}

		protected BadArgumentException badArgument(String message) {
			return new BadArgumentException(index + 1, name, message);
		}

		protected Object peek() {
			if (index < args.length) {
				return args[index];
			}
			else {
				throw new NoSuchElementException("value expected");
			}
		}

		protected Object peek(String expectedType) {
			try {
				return peek();
			}
			catch (NoSuchElementException ex) {
				throw new UnexpectedArgumentException(expectedType, "no value");
			}
		}

		// guaranteed not to return null
		protected Number peekNumber() {
			Object arg = peek(TYPENAME_NUMBER);
			Number n = Conversions.numericalValueOf(arg);
			if (n != null) {
				return n;
			}
			else {
				throw new UnexpectedArgumentException(TYPENAME_NUMBER, namer.typeNameOf(arg));
			}
		}

		public Object peekOrNil() {
			if (hasNext()) {
				return peek();
			}
			else {
				return null;
			}
		}

		public <T> T nextStrict(String expectedTypeName, Class<T> clazz) {
			final T result;
			try {
				Object arg = peek(expectedTypeName);
				if (arg != null && clazz.isAssignableFrom(arg.getClass())) {
					@SuppressWarnings("unchecked")
					T typed = (T) arg;
					result = typed;
				}
				else {
					throw new UnexpectedArgumentException(expectedTypeName, namer.typeNameOf(arg));
				}
			}
			catch (RuntimeException ex) {
				throw badArgument(ex);
			}
			skip();
			return result;
		}

		public <T> T nextStrictOrNil(String expectedTypeName, Class<T> clazz) {
			final T result;
			try {
				Object arg = peek(expectedTypeName);
				if (arg == null || clazz.isAssignableFrom(arg.getClass())) {
					@SuppressWarnings("unchecked")
					T typed = (T) arg;
					result = typed;
				}
				else {
					throw new UnexpectedArgumentException(expectedTypeName, namer.typeNameOf(arg));
				}
			}
			catch (RuntimeException ex) {
				String message = "nil or " + expectedTypeName + " expected";
				throw badArgument(message);
			}
			skip();
			return result;
		}

		public Object nextAny() {
			final Object result;
			try {
				result = peek();
			}
			catch (RuntimeException ex) {
				throw badArgument(ex);
			}
			skip();
			return result;
		}

		// does not distinguish missing value vs nil
		// this should be the last argument to check, otherwise use peekOrNil() followed by skip()
		public Object optNextAny() {
			if (hasNext()) {
				return nextAny();
			}
			else {
				return null;
			}
		}

		public Number nextNumber() {
			final Number result;
			try {
				result = peekNumber();
			}
			catch (RuntimeException ex) {
				throw badArgument(ex);
			}
			skip();
			return result;
		}

		public long nextInteger() {
			final long result;
			try {
				result = Conversions.toIntegerValue(peekNumber());
			}
			catch (RuntimeException ex) {
				throw badArgument(ex);
			}
			skip();
			return result;
		}

		public double nextFloat() {
			return nextNumber().doubleValue();
		}

		public int nextInt() {
			return (int) nextInteger();
		}

		public int optNextInt(int defaultValue) {
			if (hasNext()) {
				Object o = peek();
				if (o == null) {
					skip();
					return defaultValue;
				}
				else {
					return nextInt();
				}
			}
			else {
				return defaultValue;
			}
		}

		public int nextIntRange(String rangeName, int min, int max) {
			final int result;
			try {
				long value = Conversions.toIntegerValue(peekNumber());
				if (value >= min && value <= max) {
					result = (int) value;
				}
				else {
					throw new IndexOutOfBoundsException(rangeName + " out of range");
				}
			}
			catch (RuntimeException ex) {
				throw badArgument(ex);
			}
			skip();
			return result;
		}

		public boolean optNextBoolean(boolean defaultValue) {
			if (hasNext()) {
				Object o = peek();
				boolean result = Conversions.booleanValueOf(o);
				skip();
				return result;
			}
			else {
				return defaultValue;
			}
		}

		public String nextString() {
			final String result;
			try {
				Object arg = peek(TYPENAME_STRING);
				String v = Conversions.stringValueOf(arg);
				if (v != null) {
					result = v;
				}
				else {
					throw new UnexpectedArgumentException(TYPENAME_STRING, namer.typeNameOf(arg));
				}
			}
			catch (RuntimeException ex) {
				throw badArgument(ex);
			}
			skip();
			return result;
		}

		public String nextStrictString() {
			return nextStrict(TYPENAME_STRING, String.class);
		}

		public String optNextString(String defaultValue) {
			return hasNext() ? nextString() : defaultValue;
		}

		public Function nextFunction() {
			return nextStrict(TYPENAME_FUNCTION, Function.class);
		}

		public Table nextTable() {
			return nextStrict(TYPENAME_TABLE, Table.class);
		}

		public Table nextTableOrNil() {
			return nextStrictOrNil(TYPENAME_TABLE, Table.class);
		}

		public Coroutine nextCoroutine() {
			return nextStrict(TYPENAME_THREAD, Coroutine.class);
		}

	}

	protected abstract String name();

	@Override
	public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
		CallArguments callArgs = new CallArguments(new LibUtils.NameMetamethodValueTypeNamer(context.getState()), name(), args);
		invoke(context, callArgs);
	}

	protected abstract void invoke(ExecutionContext context, CallArguments args) throws ControlThrowable;

	@Override
	public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
		throw new NonsuspendableFunctionException(this.getClass());
	}

}

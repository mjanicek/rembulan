package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.AssertionFailedException;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.IllegalOperationAttemptException;
import net.sandius.rembulan.core.LInteger;
import net.sandius.rembulan.core.LNumber;
import net.sandius.rembulan.core.LuaRuntimeException;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.PlainValueTypeNamer;
import net.sandius.rembulan.core.Preemption;
import net.sandius.rembulan.core.ProtectedResumable;
import net.sandius.rembulan.core.RawOperators;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.impl.Varargs;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.BasicLib;
import net.sandius.rembulan.util.Check;

import java.io.PrintStream;

public class DefaultBasicLib extends BasicLib {

	private final Print print;

	public DefaultBasicLib(PrintStream out) {
		this.print = new Print(out);
	}

	@Override
	public String __VERSION() {
		return "Lua 5.3";
	}

	@Override
	public Function _print() {
		return print;
	}

	@Override
	public Function _type() {
		return Type.INSTANCE;
	}

	@Override
	public Function _next() {
		return Next.INSTANCE;
	}

	@Override
	public Function _pairs() {
		return Pairs.INSTANCE;
	}

	@Override
	public Function _ipairs() {
		return IPairs.INSTANCE;
	}

	@Override
	public Function _tostring() {
		return ToString.INSTANCE;
	}

	@Override
	public Function _tonumber() {
		return ToNumber.INSTANCE;
	}

	@Override
	public Function _error() {
		return Error.INSTANCE;
	}

	@Override
	public Function _assert() {
		return Assert.INSTANCE;
	}

	@Override
	public Function _getmetatable() {
		return GetMetatable.INSTANCE;
	}

	@Override
	public Function _setmetatable() {
		return SetMetatable.INSTANCE;
	}

	@Override
	public Function _pcall() {
		return PCall.INSTANCE;
	}

	@Override
	public Function _xpcall() {
		return XPCall.INSTANCE;
	}

	@Override
	public Function _rawequal() {
		return RawEqual.INSTANCE;
	}

	@Override
	public Function _rawget() {
		return RawGet.INSTANCE;
	}

	@Override
	public Function _rawset() {
		return RawSet.INSTANCE;
	}

	@Override
	public Function _rawlen() {
		return RawLen.INSTANCE;
	}

	@Override
	public Function _select() {
		return Select.INSTANCE;
	}

	@Override
	public Function _collectgarbage() {
		return null;  // TODO
	}

	@Override
	public Function _dofile() {
		return null;  // TODO
	}

	@Override
	public Function _load() {
		return null;  // TODO
	}

	@Override
	public Function _loadfile() {
		return null;  // TODO
	}


	public static class Print extends LibFunction {

		private final PrintStream out;

		public Print(PrintStream out) {
			this.out = Check.notNull(out);
		}

		@Override
		protected String name() {
			return "print";
		}

		private Preemption run(ExecutionContext context, Object[] args) {
			for (int i = 0; i < args.length; i++) {
				Object a = args[i];
				try {
					Dispatch.call(context, ToString.INSTANCE, a);
				}
				catch (ControlThrowable ct) {
					ct.push(this, Varargs.from(args, i + 1));
					return ct.toPreemption();
				}

				Object s = context.getObjectSink()._0();
				if (s instanceof String) {
					out.print(s);
				}
				else {
					throw new LuaRuntimeException("error calling 'print' ('tostring' must return a string to 'print')");
				}

				if (i + 1 < args.length) {
					out.print('\t');
				}
			}
			out.println();

			// returning nothing
			context.getObjectSink().setTo();
			return null;
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			return run(context, args.args);
		}

		@Override
		protected Preemption _resume(ExecutionContext context, Object suspendedState) {
			return run(context, (Object[]) suspendedState);
		}

	}

	public static class Type extends LibFunction {

		public static final Type INSTANCE = new Type();

		@Override
		protected String name() {
			return "type";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(args.nextAny());
			context.getObjectSink().setTo(typeName);
			return null;
		}

	}

	public static class Next extends LibFunction {

		public static final Next INSTANCE = new Next();

		@Override
		protected String name() {
			return "next";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Table table = args.nextTable();
			Object index = args.optNextAny();

			final Object nxt;

			if (index != null) {
				nxt = table.nextIndex(index);
			}
			else {
				nxt = table.initialIndex();
			}

			if (nxt == null) {
				// we've reached the end
				context.getObjectSink().setTo(null);
				return null;
			}
			else {
				Object value = table.rawget(nxt);
				context.getObjectSink().setTo(nxt, value);
				return null;
			}
		}

	}

	public static class INext extends LibFunction {

		public static final INext INSTANCE = new INext();

		@Override
		protected String name() {
			return "inext";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Table table = args.nextTable();
			int index = args.nextInt();

			index += 1;

			Object o = table.rawget(index);
			if (o != null) {
				context.getObjectSink().setTo(LInteger.valueOf(index), o);
				return null;
			}
			else {
				context.getObjectSink().setTo(null);
				return null;
			}
		}

	}

	public static class Pairs extends LibFunction {

		public static final Pairs INSTANCE = new Pairs();

		@Override
		protected String name() {
			return "pairs";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Table t = args.nextTable();
			Object metamethod = Metatables.getMetamethod(context.getState(), MT_PAIRS, t);

			if (metamethod != null) {
				try {
					Dispatch.call(context, metamethod, t);
				}
				catch (ControlThrowable ct) {
					ct.push(this, null);
					return ct.toPreemption();
				}

				ObjectSink os = context.getObjectSink();
				os.setTo(os._0(), os._1(), os._2());
				return null;
			}
			else {
				ObjectSink os = context.getObjectSink();
				os.setTo(Next.INSTANCE, t, null);
				return null;
			}
		}

		@Override
		protected Preemption _resume(ExecutionContext context, Object suspendedState) {
			ObjectSink os = context.getObjectSink();
			os.setTo(os._0(), os._1(), os._2());
			return null;
		}

	}

	public static class IPairs extends LibFunction {

		public static final IPairs INSTANCE = new IPairs();

		@Override
		protected String name() {
			return "ipairs";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Table t = args.nextTable();
			context.getObjectSink().setTo(INext.INSTANCE, t, LInteger.ZERO);
			return null;
		}

	}

	public static class ToString extends LibFunction {

		public static final ToString INSTANCE = new ToString();

		public static String toString(Object o) {
			return Conversions.objectToString(o);
		}

		@Override
		protected String name() {
			return "tostring";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Object arg = args.nextAny();

			Object meta = Metatables.getMetamethod(context.getState(), MT_TOSTRING, arg);
			if (meta != null) {
				try {
					Dispatch.call(context, meta, arg);
				}
				catch (ControlThrowable ct) {
					ct.push(this, null);
					return ct.toPreemption();
				}

				// resume
				return _resume(context, null);
			}
			else {
				// no metamethod, just call the default toString
				String s = toString(arg);
				context.getObjectSink().setTo(s);
				return null;
			}
		}

		@Override
		protected Preemption _resume(ExecutionContext context, Object suspendedState) {
			// trim to single value
			Object result = context.getObjectSink()._0();
			context.getObjectSink().setTo(result);
			return null;
		}

	}

	public static class ToNumber extends LibFunction {

		public static final ToNumber INSTANCE = new ToNumber();

		public static LInteger toNumber(String s, int base) {
			try {
				return LInteger.valueOf(Long.parseLong(s.trim(), base));
			}
			catch (NumberFormatException ex) {
				return null;
			}
		}

		@Override
		protected String name() {
			return "tonumber";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			if (args.size() < 2) {
				// no base
				Object o = args.nextAny();
				LNumber n = Conversions.objectAsLNumber(o);
				context.getObjectSink().setTo(n);
				return null;
			}
			else {
				// first get the base, then retrieve the string, then check the base value
				args.skip();
				int base = args.nextInt();
				args.reset();
				String s = args.nextStrictString();

				if (base < Character.MIN_RADIX || base > Character.MAX_RADIX) {
					throw new BadArgumentException(2, name(), "base out of range");
				}

				context.getObjectSink().setTo(toNumber(s, base));
				return null;
			}
		}

	}


	public static class GetMetatable extends LibFunction {

		public static final GetMetatable INSTANCE = new GetMetatable();

		@Override
		protected String name() {
			return "getmetatable";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Object arg = args.nextAny();
			Object meta = Metatables.getMetamethod(context.getState(), MT_METATABLE, arg);

			Object result = meta != null
					? meta  // __metatable field present, return its value
					: context.getState().getMetatable(arg);  // return the entire metatable

			context.getObjectSink().setTo(result);
			return null;
		}

	}

	public static class SetMetatable extends LibFunction {

		public static final SetMetatable INSTANCE = new SetMetatable();

		@Override
		protected String name() {
			return "setmetatable";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Table t = args.nextTable();
			Table mt = args.nextTableOrNil();

			if (Metatables.getMetamethod(context.getState(), MT_METATABLE, t) != null) {
				throw new IllegalOperationAttemptException("cannot change a protected metatable");
			}
			else {
				t.setMetatable(mt);
				context.getObjectSink().setTo(t);
				return null;
			}
		}

	}

	public static class Error extends LibFunction {

		public static final Error INSTANCE = new Error();

		@Override
		protected String name() {
			return "error";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			// TODO: handle levels
			Object arg1 = args.optNextAny();
			throw new LuaRuntimeException(arg1);
		}

	}

	public static class Assert extends LibFunction {

		public static final Assert INSTANCE = new Assert();

		@Override
		protected String name() {
			return "assert";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			if (Conversions.objectToBoolean(args.nextAny())) {
				context.getObjectSink().setToArray(args.getAll());
				return null;
			}
			else {
				final AssertionFailedException ex;
				if (args.hasNext()) {
					// message is defined
					Object message = args.nextAny();
					String stringMessage = Conversions.objectAsString(message);
					if (stringMessage != null) {
						ex = new AssertionFailedException(stringMessage);
					}
					else {
						ex = new AssertionFailedException(message);
					}
				}
				else {
					// message not defined, use the default
					ex = new AssertionFailedException("assertion failed!");
				}

				throw ex;
			}

		}

	}

	public static class PCall extends LibFunction implements ProtectedResumable {

		public static final PCall INSTANCE = new PCall();

		@Override
		protected String name() {
			return "pcall";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Object callTarget = args.nextAny();
			Object[] callArgs = args.getTail();

			try {
				Dispatch.call(context, callTarget, callArgs);
			}
			catch (ControlThrowable ct) {
				ct.push(this, null);
				return ct.toPreemption();
			}
			catch (Exception ex) {
				context.getObjectSink().setTo(Boolean.FALSE, Conversions.throwableToObject(ex));  // failure
				return null;
			}

			context.getObjectSink().prepend(Boolean.TRUE);  // success
			return null;
		}

		@Override
		protected Preemption _resume(ExecutionContext context, Object suspendedState) {
			// success
			context.getObjectSink().prepend(Boolean.TRUE);
			return null;
		}

		@Override
		public void resumeError(ExecutionContext context, Object suspendedState, Object error) throws ControlThrowable {
			context.getObjectSink().setTo(Boolean.FALSE, error);
		}

	}

	public static class XPCall extends LibFunction implements ProtectedResumable {

		public static final int MAX_DEPTH = 220;  // 220 in PUC-Lua 5.3

		public static final XPCall INSTANCE = new XPCall();

		@Override
		protected String name() {
			return "xpcall";
		}

		private static class SavedState {
			public final Function handler;
			public final int depth;

			private SavedState(Function handler, int depth) {
				this.handler = handler;
				this.depth = depth;
			}
		}

		private void prependTrue(ExecutionContext context) {
			context.getObjectSink().prepend(Boolean.TRUE);
		}

		private void prependFalseAndTrim(ExecutionContext context) {
			ObjectSink os = context.getObjectSink();
			Object errorObject = os._0();
			os.setTo(Boolean.FALSE, errorObject);
		}

		private Preemption handleError(ExecutionContext context, Function handler, int depth, Object errorObject) {
			// we want to be able to handle nil error objects, so we need a separate flag
			boolean isError = true;

			while (isError && depth < MAX_DEPTH) {
				depth += 1;

				try {
					Dispatch.call(context, handler, errorObject);
					isError = false;
				}
				catch (ControlThrowable ct) {
					ct.push(this, new SavedState(handler, depth));
					return ct.toPreemption();
				}
				catch (Exception e) {
					errorObject = Conversions.throwableToObject(e);
					isError = true;
				}
			}

			if (!isError) {
				prependFalseAndTrim(context);
				return null;
			}
			else {
				// depth must be >= MAX_DEPTH
				context.getObjectSink().setTo(Boolean.FALSE, "error in error handling");
				return null;
			}
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Object callTarget = args.optNextAny();
			Function handler = args.nextFunction();
			Object[] callArgs = args.getTail();

			Object errorObject = null;
			boolean isError = false;  // need to distinguish nil error objects from no-error

			try {
				Dispatch.call(context, callTarget, callArgs);
			}
			catch (ControlThrowable ct) {
				ct.push(this, new SavedState(handler, 0));
			}
			catch (Exception e) {
				errorObject = Conversions.throwableToObject(e);
				isError = true;
			}

			if (!isError) {
				prependTrue(context);
				return null;
			}
			else {
				return handleError(context, handler, 0, errorObject);
			}
		}

		@Override
		protected Preemption _resume(ExecutionContext context, Object suspendedState) {
			SavedState ss = (SavedState) suspendedState;
			if (ss.depth == 0) {
				prependTrue(context);
				return null;
			}
			else {
				prependFalseAndTrim(context);
				return null;
			}
		}

		@Override
		public void resumeError(ExecutionContext context, Object suspendedState, Object error) throws ControlThrowable {
			SavedState ss = (SavedState) suspendedState;
			Preemption p = handleError(context, ss.handler, ss.depth, error);
			if (p != null) {
				throw p.toControlThrowable();
			}
		}

	}

	public static class RawEqual extends LibFunction {

		public static final RawEqual INSTANCE = new RawEqual();

		@Override
		protected String name() {
			return "rawequal";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Object a = args.nextAny();
			Object b = args.nextAny();
			context.getObjectSink().setTo(RawOperators.raweq(a, b));
			return null;
		}

	}

	public static class RawGet extends LibFunction {

		public static final RawGet INSTANCE = new RawGet();

		@Override
		protected String name() {
			return "rawget";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Table table = args.nextTable();
			Object key = args.nextAny();
			context.getObjectSink().setTo(table.rawget(key));
			return null;
		}

	}

	public static class RawSet extends LibFunction {

		public static final RawSet INSTANCE = new RawSet();

		@Override
		protected String name() {
			return "rawset";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Table table = args.nextTable();
			Object key = args.nextAny();
			Object value = args.nextAny();

			table.rawset(key, value);
			context.getObjectSink().setTo(table);
			return null;
		}

	}

	public static class RawLen extends LibFunction {

		public static final RawLen INSTANCE = new RawLen();

		@Override
		protected String name() {
			return "rawlen";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			final long result;

			// no need to distinguish missing value vs nil
			Object arg1 = args.optNextAny();

			if (arg1 instanceof Table) {
				Table table = (Table) arg1;
				result = table.rawlen();
			}
			else if (arg1 instanceof String) {
				String s = (String) arg1;
				result = RawOperators.stringLen(s);
			}
			else {
				throw new BadArgumentException(1, name(), "table or string expected");
			}

			context.getObjectSink().setTo(LInteger.valueOf(result));
			return null;
		}

	}

	public static class Select extends LibFunction {

		public static final Select INSTANCE = new Select();

		@Override
		protected String name() {
			return "select";
		}

		@Override
		protected Preemption invoke(ExecutionContext context, CallArguments args) {
			Object index = args.optNextAny();

			if (index instanceof String && ((String) index).startsWith("#")) {
				// return the number of remaining args
				context.getObjectSink().setTo(LInteger.valueOf(args.tailSize()));
				return null;
			}
			else {
				args.reset();
				int idx = args.nextIntRange("index", -args.size() + 1, Integer.MAX_VALUE);

				int from = idx >= 0
						? idx  // from the beginning
						: args.size() + idx;  // idx < 0: from the end (-1 is the last index)

				if (from < 1) {
					throw new BadArgumentException(1, name(), "index out of range");
				}

				context.getObjectSink().setToArray(Varargs.from(args.getAll(), from));
				return null;
			}
		}

	}

}

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.LuaType;
import net.sandius.rembulan.core.AssertionFailedException;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaRuntimeException;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.ProtectedResumable;
import net.sandius.rembulan.core.RawOperators;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Value;
import net.sandius.rembulan.core.impl.Function1;
import net.sandius.rembulan.core.impl.Function2;
import net.sandius.rembulan.core.impl.FunctionAnyarg;
import net.sandius.rembulan.core.impl.Varargs;
import net.sandius.rembulan.lib.BasicLib;
import net.sandius.rembulan.lib.LibUtils;
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
		return null;  // TODO
	}

	@Override
	public Function _tostring() {
		return ToString.INSTANCE;
	}

	@Override
	public Function _tonumber() {
		return null;  // TODO
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
		return null;  // TODO
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


	// TODO: use tostring to convert arguments to string -- may involve calling metamethods, i.e. needs to be resumable
	public static class Print extends FunctionAnyarg {

		private final PrintStream out;

		public Print(PrintStream out) {
			this.out = Check.notNull(out);
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			for (int i = 0; i < args.length; i++) {
				out.print(ToString.toString(args[i]));
				if (i + 1 < args.length) {
					out.print('\t');
				}
			}
			out.println();
			context.getObjectSink().setTo();
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Type extends Function1 {

		public static final Type INSTANCE = new Type();

		@Override
		public void invoke(ExecutionContext context, Object arg) throws ControlThrowable {
			LuaType tpe = Value.typeOf(arg);
			context.getObjectSink().setTo(tpe.name);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Next extends FunctionAnyarg {

		public static final Next INSTANCE = new Next();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Table table = LibUtils.checkTable("next", args, 0);
			Object index = Varargs.getElement(args, 1);

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
			}
			else {
				Object value = table.rawget(nxt);
				context.getObjectSink().setTo(nxt, value);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Pairs extends FunctionAnyarg {

		public static final Pairs INSTANCE = new Pairs();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Table t = LibUtils.checkTable("pairs", args, 0);
			Object metamethod = Metatables.getMetamethod(context.getState(), "__pairs", t);

			if (metamethod != null) {
				try {
					Dispatch.call(context, metamethod, t);
				}
				catch (ControlThrowable ct) {
					ct.push(this, null);
					throw ct;
				}

				ObjectSink os = context.getObjectSink();
				os.setTo(os._0(), os._1(), os._2());
			}
			else {
				ObjectSink os = context.getObjectSink();
				os.setTo(Next.INSTANCE, t, null);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			ObjectSink os = context.getObjectSink();
			os.setTo(os._0(), os._1(), os._2());
		}

	}

	// TODO: handle the __tostring metamethod
	public static class ToString extends Function1 {

		public static final ToString INSTANCE = new ToString();

		public static String toString(Object o) {
			return Conversions.objectToString(o);
		}

		@Override
		public void invoke(ExecutionContext context, Object arg) throws ControlThrowable {
			context.getObjectSink().setTo(toString(arg));
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	// TODO: handle the __metatable field
	public static class GetMetatable extends Function1 {

		public static final GetMetatable INSTANCE = new GetMetatable();

		@Override
		public void invoke(ExecutionContext context, Object arg) throws ControlThrowable {
			context.getObjectSink().setTo(Metatables.getMetatable(context.getState(), arg));
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	// TODO: throw error if the original metatable has the __metatable field
	public static class SetMetatable extends Function2 {

		public static final SetMetatable INSTANCE = new SetMetatable();

		@Override
		public void invoke(ExecutionContext context, Object arg1, Object arg2) throws ControlThrowable {
			Table t = LibUtils.checkArgument(arg1, 0, Table.class);
			Table mt = LibUtils.checkArgumentOrNil(arg2, 1, Table.class);

			t.setMetatable(mt);
			context.getObjectSink().setTo(t);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Error extends Function2 {

		public static final Error INSTANCE = new Error();

		@Override
		public void invoke(ExecutionContext context, Object arg1, Object arg2) throws ControlThrowable {
			// TODO: handle levels
			throw new LuaRuntimeException(arg1);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Assert extends FunctionAnyarg {

		public static final Assert INSTANCE = new Assert();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			if (Conversions.objectToBoolean(Varargs.getElement(args, 0))) {
				context.getObjectSink().setToArray(args);
			}
			else {
				final AssertionFailedException ex;
				if (args.length > 1) {
					// message is defined
					Object message = args[1];
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

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class PCall extends FunctionAnyarg implements ProtectedResumable {

		public static final PCall INSTANCE = new PCall();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Object callTarget = Varargs.getElement(args, 0);
			Object[] callArgs = Varargs.from(args, 1);

			try {
				Dispatch.call(context, callTarget, callArgs);
			}
			catch (ControlThrowable ct) {
				ct.push(this, null);
				throw ct;
			}
			catch (Exception ex) {
				context.getObjectSink().setTo(Boolean.FALSE, Conversions.throwableToObject(ex));  // failure
				return;
			}

			context.getObjectSink().prepend(Boolean.TRUE);  // success
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			// success
			context.getObjectSink().prepend(Boolean.TRUE);
		}

		@Override
		public void resumeError(ExecutionContext context, Object suspendedState, Object error) throws ControlThrowable {
			context.getObjectSink().setTo(Boolean.FALSE, error);
		}

	}

	public static class XPCall extends FunctionAnyarg implements ProtectedResumable {

		public static final int MAX_DEPTH = 220;  // 220 in PUC-Lua 5.3

		public static final XPCall INSTANCE = new XPCall();

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

		private void prependFalseAndPad(ExecutionContext context) {
			ObjectSink os = context.getObjectSink();
			if (os.size() == 0) {
				// if empty, pad with a dummy nil value
				os.setTo(Boolean.FALSE, null);
			}
			else {
				// just prepend false
				os.prepend(Boolean.FALSE);
			}
		}

		private void handleError(ExecutionContext context, Function handler, int depth, Object errorObject) throws ControlThrowable {
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
					throw ct;
				}
				catch (Exception e) {
					errorObject = Conversions.throwableToObject(e);
					isError = true;
				}
			}

			if (!isError) {
				prependFalseAndPad(context);
			}
			else {
				// depth must be >= MAX_DEPTH
				context.getObjectSink().setTo(Boolean.FALSE, "error in error handling");
			}
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Function handler = LibUtils.checkFunction("xpcall", args, 1);
			Object callTarget = LibUtils.checkValue("xpcall", args, 0);
			Object[] callArgs = Varargs.from(args, 2);

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
			}
			else {
				handleError(context, handler, 0, errorObject);
			}
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			SavedState ss = (SavedState) suspendedState;
			if (ss.depth == 0) {
				prependTrue(context);
			}
			else {
				prependFalseAndPad(context);
			}
		}

		@Override
		public void resumeError(ExecutionContext context, Object suspendedState, Object error) throws ControlThrowable {
			SavedState ss = (SavedState) suspendedState;
			handleError(context, ss.handler, ss.depth, error);
		}

	}

	public static class RawEqual extends FunctionAnyarg {

		public static final RawEqual INSTANCE = new RawEqual();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Object a = LibUtils.checkValue("rawequal", args, 0);
			Object b = LibUtils.checkValue("rawequal", args, 1);

			context.getObjectSink().setTo(RawOperators.raweq(a, b));
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class RawGet extends FunctionAnyarg {

		public static final RawGet INSTANCE = new RawGet();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Table table = LibUtils.checkTable("rawget", args, 0);
			Object key = LibUtils.checkValue("rawget", args, 1);

			context.getObjectSink().setTo(table.rawget(key));
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class RawSet extends FunctionAnyarg {

		public static final RawSet INSTANCE = new RawSet();

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			Table table = LibUtils.checkTable("rawset", args, 0);
			Object key = LibUtils.checkValue("rawset", args, 1);
			Object value = LibUtils.checkValue("rawset", args, 2);

			table.rawset(key, value);
			context.getObjectSink().setTo(table);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class RawLen extends Function1 {

		public static final RawLen INSTANCE = new RawLen();

		@Override
		public void invoke(ExecutionContext context, Object arg1) throws ControlThrowable {
			final long result;

			if (arg1 instanceof Table) {
				Table table = (Table) arg1;
				result = table.rawlen();
			}
			else if (arg1 instanceof String) {
				String s = (String) arg1;
				result = RawOperators.stringLen(s);
			}
			else {
				throw new IllegalArgumentException("bad argument #1 to 'rawlen' (table or string expected)");
			}

			context.getObjectSink().setTo(result);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

}

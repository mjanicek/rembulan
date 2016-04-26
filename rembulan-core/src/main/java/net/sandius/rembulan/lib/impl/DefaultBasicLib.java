package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.LuaType;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
import net.sandius.rembulan.core.ProtectedResumable;
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
import java.io.Serializable;

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
		return null;  // TODO
	}

	@Override
	public Function _pairs() {
		return null;  // TODO
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
		return null;  // TODO
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
		return null;  // TODO
	}

	@Override
	public Function _rawequal() {
		return null;  // TODO
	}

	@Override
	public Function _rawget() {
		return null;  // TODO
	}

	@Override
	public Function _rawset() {
		return null;  // TODO
	}

	@Override
	public Function _rawlen() {
		return null;  // TODO
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


	public static class Print extends FunctionAnyarg {

		private final PrintStream out;

		public Print(PrintStream out) {
			this.out = Check.notNull(out);
		}

		@Override
		public void invoke(ExecutionContext context, Object[] args) throws ControlThrowable {
			for (int i = 0; i < args.length; i++) {
				if (i + 1 < args.length) {
					out.print('\t');
				}
				out.print(ToString.toString(args[i]));
			}
			out.println();
			context.getObjectSink().reset();
		}

		@Override
		public void resume(ExecutionContext context, Serializable suspendedState) throws ControlThrowable {
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
		public void resume(ExecutionContext context, Serializable suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class ToString extends Function1 {

		public static final ToString INSTANCE = new ToString();

		public static String toString(Object o) {
			String s = Conversions.objectAsString(o);
			return s != null ? s : LuaFormat.NIL;
		}

		@Override
		public void invoke(ExecutionContext context, Object arg) throws ControlThrowable {
			context.getObjectSink().setTo(toString(arg));
		}

		@Override
		public void resume(ExecutionContext context, Serializable suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class GetMetatable extends Function1 {

		public static final GetMetatable INSTANCE = new GetMetatable();

		@Override
		public void invoke(ExecutionContext context, Object arg) throws ControlThrowable {
			context.getObjectSink().setTo(Metatables.getMetatable(context.getState(), arg));
		}

		@Override
		public void resume(ExecutionContext context, Serializable suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

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
		public void resume(ExecutionContext context, Serializable suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Assert extends Function1 {

		public static final Assert INSTANCE = new Assert();

		@Override
		public void invoke(ExecutionContext context, Object arg1) throws ControlThrowable {
			if (!Conversions.objectToBoolean(arg1)) {
				throw new IllegalStateException("assertion failed!");
			}
			context.getObjectSink().reset();
		}

		@Override
		public void resume(ExecutionContext context, Serializable suspendedState) throws ControlThrowable {
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
				context.getObjectSink().setTo(false, Conversions.throwableToObject(ex));  // failure
			}

			context.getObjectSink().prepend(new Object[] {true});  // success
		}

		@Override
		public void resume(ExecutionContext context, Serializable suspendedState) throws ControlThrowable {
			// success
			context.getObjectSink().prepend(new Object[] {true});
		}

		@Override
		public void resumeError(ExecutionContext context, Serializable suspendedState, Object error) throws ControlThrowable {
			context.getObjectSink().setTo(false, error);
		}

	}

}

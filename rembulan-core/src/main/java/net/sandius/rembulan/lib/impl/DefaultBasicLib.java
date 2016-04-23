package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.LuaType;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Value;
import net.sandius.rembulan.core.impl.Function1;
import net.sandius.rembulan.core.impl.Function2;
import net.sandius.rembulan.core.impl.FunctionAnyarg;
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
		return null;  // TODO
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
		public void invoke(LuaState state, ObjectSink result, Object[] args) throws ControlThrowable {
			for (int i = 0; i < args.length; i++) {
				if (i + 1 < args.length) {
					out.print('\t');
				}
				out.print(ToString.toString(args[i]));
			}
			out.println();
			result.reset();
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Serializable suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Type extends Function1 {

		public static final Type INSTANCE = new Type();

		@Override
		public void invoke(LuaState state, ObjectSink result, Object arg) throws ControlThrowable {
			LuaType tpe = Value.typeOf(arg);
			result.setTo(tpe.name);
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Serializable suspendedState) throws ControlThrowable {
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
		public void invoke(LuaState state, ObjectSink result, Object arg) throws ControlThrowable {
			result.setTo(toString(arg));
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Serializable suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class GetMetatable extends Function1 {

		public static final GetMetatable INSTANCE = new GetMetatable();

		@Override
		public void invoke(LuaState state, ObjectSink result, Object arg) throws ControlThrowable {
			result.setTo(Metatables.getMetatable(state, arg));
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Serializable suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class SetMetatable extends Function2 {

		public static final SetMetatable INSTANCE = new SetMetatable();

		@Override
		public void invoke(LuaState state, ObjectSink result, Object arg1, Object arg2) throws ControlThrowable {
			Table t = LibUtils.checkArgument(arg1, 0, Table.class);
			Table mt = LibUtils.checkArgumentOrNil(arg2, 1, Table.class);

			t.setMetatable(mt);
			result.setTo(t);
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Serializable suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

	public static class Assert extends Function1 {

		public static final Assert INSTANCE = new Assert();

		@Override
		public void invoke(LuaState state, ObjectSink result, Object arg1) throws ControlThrowable {
			if (!Conversions.objectToBoolean(arg1)) {
				throw new IllegalStateException("assertion failed!");
			}
			result.reset();
		}

		@Override
		public void resume(LuaState state, ObjectSink result, Serializable suspendedState) throws ControlThrowable {
			throw new NonsuspendableFunctionException(this.getClass());
		}

	}

}

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.lib.DebugLib;

public class DefaultDebugLib extends DebugLib {

	@Override
	public Function _debug() {
		return null;  // TODO
	}

	@Override
	public Function _gethook() {
		return null;  // TODO
	}

	@Override
	public Function _getinfo() {
		return null;  // TODO
	}

	@Override
	public Function _getlocal() {
		return null;  // TODO
	}

	@Override
	public Function _getmetatable() {
		return GetMetatable.INSTANCE;
	}

	@Override
	public Function _getregistry() {
		return null;  // TODO
	}

	@Override
	public Function _getupvalue() {
		return null;  // TODO
	}

	@Override
	public Function _getuservalue() {
		return null;  // TODO
	}

	@Override
	public Function _sethook() {
		return null;  // TODO
	}

	@Override
	public Function _setlocal() {
		return null;  // TODO
	}

	@Override
	public Function _setmetatable() {
		return SetMetatable.INSTANCE;
	}

	@Override
	public Function _setupvalue() {
		return null;  // TODO
	}

	@Override
	public Function _setuservalue() {
		return null;  // TODO
	}

	@Override
	public Function _traceback() {
		return null;  // TODO
	}

	@Override
	public Function _upvalueid() {
		return null;  // TODO
	}

	@Override
	public Function _upvaluejoin() {
		return null;  // TODO
	}


	public static class GetMetatable extends LibFunction {

		public static final GetMetatable INSTANCE = new GetMetatable();

		@Override
		protected String name() {
			return "getmetatable";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Object value = args.nextAny();
			Table mt = context.getState().getMetatable(value);
			context.getObjectSink().setTo(mt);
		}

	}

	public static class SetMetatable extends LibFunction {

		public static final SetMetatable INSTANCE = new SetMetatable();

		@Override
		protected String name() {
			return "setmetatable";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Object value = args.peekOrNil();
			args.skip();
			Table mt = args.nextTableOrNil();

			// set the new metatable
			context.getState().setMetatable(value, mt);

			// return value
			context.getObjectSink().setTo(value);
		}

	}

}

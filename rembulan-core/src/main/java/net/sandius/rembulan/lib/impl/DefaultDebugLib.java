package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaRuntimeException;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.lib.DebugLib;
import net.sandius.rembulan.util.Check;

import java.lang.reflect.Field;

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
		return GetUpvalue.INSTANCE;
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

	public static class GetUpvalue extends LibFunction {

		public static final GetUpvalue INSTANCE = new GetUpvalue();

		@Override
		protected String name() {
			return "getupvalue";
		}

		private static Field findUpvalueField(Function f, int index) {
			Check.notNull(f);

			// find the index-th upvalue field
			int idx = 0;
			for (Field fld : f.getClass().getDeclaredFields()) {
				Class<?> fldType = fld.getType();
				if (Upvalue.class.isAssignableFrom(fldType)) {
					if (idx == index) {
						// found it
						return fld;
					}
					else {
						idx += 1;
					}
				}
			}

			return null;
		}

		private static String upvalueName(Field field) {
			Check.notNull(field);
			return field.getName();
		}

		private static Upvalue upvalueInstance(Field field, Function instance) throws IllegalAccessException {
			Check.notNull(field);
			Check.notNull(instance);
			field.setAccessible(true);
			return (Upvalue) field.get(instance);
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			args.skip();
			int index = args.nextInt();
			args.rewind();
			Function f = args.nextFunction();

			Field upvalueField = findUpvalueField(f, index - 1);

			if (upvalueField != null) {
				final Object value;
				try {
					Upvalue uv = upvalueInstance(upvalueField, f);
					value = uv.get();  // assuming uv to be non-null
				}
				catch (IllegalAccessException ex) {
					// shouldn't happen (we set the field accessor's accessible flag to true)
					throw new LuaRuntimeException(ex);
				}

				String name = upvalueName(upvalueField);

				context.getObjectSink().setTo(name, value);
			}
			else {
				// contrary to what its documentation says, PUC-Lua 5.3.2 doesn't seem to return
				// nil but rather an empty list
				context.getObjectSink().reset();
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

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaRuntimeException;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.core.Userdata;
import net.sandius.rembulan.lib.BadArgumentException;
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
		return GetUserValue.INSTANCE;
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
		return SetUpvalue.INSTANCE;
	}

	@Override
	public Function _setuservalue() {
		return SetUserValue.INSTANCE;
	}

	@Override
	public Function _traceback() {
		return null;  // TODO
	}

	@Override
	public Function _upvalueid() {
		return UpvalueId.INSTANCE;
	}

	@Override
	public Function _upvaluejoin() {
		return UpvalueJoin.INSTANCE;
	}


	private static class UpvalueRef {

		private final int index;
		private final Function function;
		private final Field field;

		public UpvalueRef(int index, Function function, Field field) {
			this.index = index;
			this.function = Check.notNull(function);
			this.field = Check.notNull(field);
		}

		// index is 0-based
		public static UpvalueRef find(Function f, int index) {
			Check.notNull(f);

			// find the index-th upvalue field
			int idx = 0;
			for (Field fld : f.getClass().getDeclaredFields()) {
				Class<?> fldType = fld.getType();
				if (Upvalue.class.isAssignableFrom(fldType)) {
					if (idx == index) {
						// found it
						fld.setAccessible(true);
						return new UpvalueRef(index, f, fld);
					}
					else {
						idx += 1;
					}
				}
			}

			return null;
		}

		public String name() {
			return field.getName();
		}

		public int index() {
			return index;
		}

		public Upvalue get() throws IllegalAccessException {
			return (Upvalue) field.get(function);
		}

		public void set(Upvalue ref) throws IllegalAccessException {
			Check.notNull(ref);
			field.set(function, ref);
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

	public static class GetUpvalue extends LibFunction {

		public static final GetUpvalue INSTANCE = new GetUpvalue();

		@Override
		protected String name() {
			return "getupvalue";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			args.skip();
			int index = args.nextInt();
			args.rewind();
			Function f = args.nextFunction();

			UpvalueRef uvRef = UpvalueRef.find(f, index - 1);

			if (uvRef != null) {
				final String name;
				final Object value;

				try {
					name = uvRef.name();
					Upvalue uv = uvRef.get();
					value = uv.get();
				}
				catch (IllegalAccessException ex) {
					throw new LuaRuntimeException(ex);
				}

				context.getObjectSink().setTo(name, value);
			}
			else {
				// contrary to what its documentation says, PUC-Lua 5.3.2 doesn't seem to return
				// nil but rather an empty list
				context.getObjectSink().reset();
			}

		}

	}

	public static class SetUpvalue extends LibFunction {

		public static final SetUpvalue INSTANCE = new SetUpvalue();

		@Override
		protected String name() {
			return "setupvalue";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			args.skip();
			args.skip();
			Object newValue = args.nextAny();
			args.rewind();
			args.skip();
			int index = args.nextInt();
			args.rewind();
			Function f = args.nextFunction();

			UpvalueRef uvRef = UpvalueRef.find(f, index - 1);

			final String name;

			if (uvRef != null) {
				try {
					name = uvRef.name();
					Upvalue uv = uvRef.get();
					uv.set(newValue);
				}
				catch (IllegalAccessException ex) {
					throw new LuaRuntimeException(ex);
				}
			}
			else {
				name = null;
			}

			context.getObjectSink().setTo(name);
		}

	}

	public static class UpvalueId extends LibFunction {

		public static final UpvalueId INSTANCE = new UpvalueId();

		@Override
		protected String name() {
			return "upvalueid";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			args.goTo(1);
			int n = args.nextInt();
			args.goTo(0);
			Function f = args.nextFunction();

			UpvalueRef uvRef = UpvalueRef.find(f, n - 1);
			if (uvRef == null) {
				throw new BadArgumentException(2, name(), "invalid upvalue index");
			}
			else {
				final Upvalue uv;
				try {
					uv = uvRef.get();
				}
				catch (IllegalAccessException ex) {
					throw new LuaRuntimeException(ex);
				}

				context.getObjectSink().setTo(uv);
			}
		}

	}

	public static class UpvalueJoin extends LibFunction {

		public static final UpvalueJoin INSTANCE = new UpvalueJoin();

		@Override
		protected String name() {
			return "upvaluejoin";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			// read f1, n1
			args.goTo(1);
			int n1 = args.nextInt();
			args.goTo(0);
			Function f1 = args.nextFunction();

			UpvalueRef uvRef1 = UpvalueRef.find(f1, n1 - 1);
			if (uvRef1 == null) {
				throw new BadArgumentException(2, name(), "invalid upvalue index");
			}

			// read f2, n2
			args.goTo(3);
			int n2 = args.nextInt();
			args.goTo(2);
			Function f2 = args.nextFunction();

			UpvalueRef uvRef2 = UpvalueRef.find(f2, n2 - 1);
			if (uvRef2 == null) {
				throw new BadArgumentException(4, name(), "invalid upvalue index");
			}

			try {
				uvRef1.set(uvRef2.get());
			}
			catch (IllegalAccessException ex) {
				throw new LuaRuntimeException(ex);
			}

			context.getObjectSink().reset();
		}

	}

	public static class GetUserValue extends LibFunction {

		public static final GetUserValue INSTANCE = new GetUserValue();

		@Override
		protected String name() {
			return "getuservalue";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Object o = args.peekOrNil();

			Object result = o instanceof Userdata ? ((Userdata) o).getUserValue() : null;
			context.getObjectSink().setTo(result);
		}

	}

	public static class SetUserValue extends LibFunction {

		public static final SetUserValue INSTANCE = new SetUserValue();

		@Override
		protected String name() {
			return "setuservalue";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Userdata userdata = args.nextUserdata();
			Object value = args.nextAny();

			userdata.setUserValue(value);
			context.getObjectSink().setTo(userdata);
		}

	}

}

/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.lib.impl;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaRuntimeException;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Userdata;
import net.sandius.rembulan.core.Variable;
import net.sandius.rembulan.core.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.BadArgumentException;
import net.sandius.rembulan.lib.DebugLib;
import net.sandius.rembulan.util.Check;

import java.lang.reflect.Field;

public class DefaultDebugLib extends DebugLib {

	private final Function _debug;
	private final Function _gethook;
	private final Function _getinfo;
	private final Function _getlocal;
	private final Function _getregistry;
	private final Function _sethook;
	private final Function _setlocal;
	private final Function _traceback;

	public DefaultDebugLib() {
		this._debug = new UnimplementedFunction("debug.debug");  // TODO
		this._gethook = new UnimplementedFunction("debug.gethook");  // TODO
		this._getinfo = new UnimplementedFunction("debug.getinfo");  // TODO
		this._getlocal = new UnimplementedFunction("debug.getlocal");  // TODO
		this._getregistry = new UnimplementedFunction("debug.getregistry");  // TODO
		this._sethook = new UnimplementedFunction("debug.sethook");  // TODO
		this._setlocal = new UnimplementedFunction("debug.setlocal");  // TODO
		this._traceback = new UnimplementedFunction("debug.traceback");  // TODO
	}

	@Override
	public Function _debug() {
		return _debug;
	}

	@Override
	public Function _gethook() {
		return _gethook;
	}

	@Override
	public Function _getinfo() {
		return _getinfo;
	}

	@Override
	public Function _getlocal() {
		return _getlocal;
	}

	@Override
	public Function _getmetatable() {
		return GetMetatable.INSTANCE;
	}

	@Override
	public Function _getregistry() {
		return _getregistry;
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
		return _sethook;
	}

	@Override
	public Function _setlocal() {
		return _setlocal;
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
		return _traceback;
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
				if (Variable.class.isAssignableFrom(fldType)) {
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

		public Variable get() throws IllegalAccessException {
			return (Variable) field.get(function);
		}

		public void set(Variable ref) throws IllegalAccessException {
			Check.notNull(ref);
			field.set(function, ref);
		}

	}

	public static class GetMetatable extends AbstractLibFunction {

		public static final GetMetatable INSTANCE = new GetMetatable();

		@Override
		protected String name() {
			return "getmetatable";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Object value = args.nextAny();
			Table mt = context.getState().getMetatable(value);
			context.getReturnVector().setTo(mt);
		}

	}

	public static class SetMetatable extends AbstractLibFunction {

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
			context.getReturnVector().setTo(value);
		}

	}

	public static class GetUpvalue extends AbstractLibFunction {

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
					Variable uv = uvRef.get();
					value = uv.get();
				}
				catch (IllegalAccessException ex) {
					throw new LuaRuntimeException(ex);
				}

				context.getReturnVector().setTo(name, value);
			}
			else {
				// contrary to what its documentation says, PUC-Lua 5.3.2 doesn't seem to return
				// nil but rather an empty list
				context.getReturnVector().reset();
			}

		}

	}

	public static class SetUpvalue extends AbstractLibFunction {

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
					Variable uv = uvRef.get();
					uv.set(newValue);
				}
				catch (IllegalAccessException ex) {
					throw new LuaRuntimeException(ex);
				}
			}
			else {
				name = null;
			}

			context.getReturnVector().setTo(name);
		}

	}

	public static class UpvalueId extends AbstractLibFunction {

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
				final Variable uv;
				try {
					uv = uvRef.get();
				}
				catch (IllegalAccessException ex) {
					throw new LuaRuntimeException(ex);
				}

				context.getReturnVector().setTo(uv);
			}
		}

	}

	public static class UpvalueJoin extends AbstractLibFunction {

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

			context.getReturnVector().reset();
		}

	}

	public static class GetUserValue extends AbstractLibFunction {

		public static final GetUserValue INSTANCE = new GetUserValue();

		@Override
		protected String name() {
			return "getuservalue";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			Object o = args.peekOrNil();

			Object result = o instanceof Userdata ? ((Userdata) o).getUserValue() : null;
			context.getReturnVector().setTo(result);
		}

	}

	public static class SetUserValue extends AbstractLibFunction {

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
			context.getReturnVector().setTo(userdata);
		}

	}

}

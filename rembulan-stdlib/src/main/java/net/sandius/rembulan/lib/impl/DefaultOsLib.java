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

import net.sandius.rembulan.env.RuntimeEnvironment;
import net.sandius.rembulan.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.OsLib;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;

import java.util.Objects;

public class DefaultOsLib extends OsLib {

	private final LuaFunction _clock;
	private final LuaFunction _date;
	private final LuaFunction _difftime;
	private final LuaFunction _execute;
	private final LuaFunction _exit;
	private final LuaFunction _getenv;
	private final LuaFunction _remove;
	private final LuaFunction _rename;
	private final LuaFunction _setlocale;
	private final LuaFunction _time;
	private final LuaFunction _tmpname;
	
	public DefaultOsLib(RuntimeEnvironment environment) {
		this._clock = new UnimplementedFunction("os.clock");  // TODO
		this._date = new UnimplementedFunction("os.date");  // TODO
		this._difftime = new UnimplementedFunction("os.difftime");  // TODO
		this._execute = new UnimplementedFunction("os.execute");  // TODO
		this._exit = new UnimplementedFunction("os.exit");  // TODO
		this._getenv = environment != null ? new GetEnv(environment) : new UnimplementedFunction("os.getenv");
		this._remove = new UnimplementedFunction("os.remove");  // TODO
		this._rename = new UnimplementedFunction("os.rename");  // TODO
		this._setlocale = new UnimplementedFunction("os.setlocale");  // TODO
		this._time = new UnimplementedFunction("os.time");  // TODO
		this._tmpname = new UnimplementedFunction("os.tmpname");  // TODO
	}

	@Deprecated
	public DefaultOsLib() {
		this(null);
	}
	
	@Override
	public LuaFunction _clock() {
		return _clock;
	}

	@Override
	public LuaFunction _date() {
		return _date;
	}

	@Override
	public LuaFunction _difftime() {
		return _difftime;
	}

	@Override
	public LuaFunction _execute() {
		return _execute;
	}

	@Override
	public LuaFunction _exit() {
		return _exit;
	}

	@Override
	public LuaFunction _getenv() {
		return _getenv;
	}

	@Override
	public LuaFunction _remove() {
		return _remove;
	}

	@Override
	public LuaFunction _rename() {
		return _rename;
	}

	@Override
	public LuaFunction _setlocale() {
		return _setlocale;
	}

	@Override
	public LuaFunction _time() {
		return _time;
	}

	@Override
	public LuaFunction _tmpname() {
		return _tmpname;
	}


	public static class GetEnv extends AbstractLibFunction {

		private final RuntimeEnvironment environment;

		public GetEnv(RuntimeEnvironment environment) {
			this.environment = Objects.requireNonNull(environment);
		}

		@Override
		protected String name() {
			return "getenv";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			String name = args.nextString();
			String value = environment.getEnv(name);
			context.getReturnBuffer().setTo(value);
		}

	}

}

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

import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.Lib;
import net.sandius.rembulan.lib.ModuleLib;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.sandius.rembulan.util.Check;

public class DefaultModuleLib extends ModuleLib {

	private final StateContext context;
	private final Table env;

	private final Table _loaded;

	private final LuaFunction _require;
	private final LuaFunction _loadlib;
	private final LuaFunction _searchpath;

	public DefaultModuleLib(StateContext context, Table env) {
		this.context = Check.notNull(context);
		this.env = Check.notNull(env);

		this._loaded = context.newTable();

		this._require = new Require();

		this._loadlib = new UnimplementedFunction("package.loadlib");
		this._searchpath = new UnimplementedFunction("package.searchpath");
	}

	@Override
	public void postInstall(StateContext context, Table env, Table libTable) {
		_loaded.rawset(name(), libTable);
	}

	@Override
	public void install(Lib lib) {
		lib.preInstall(context, env);
		Table t = lib.toTable(context);
		if (t != null) {
			String name = lib.name();
			env.rawset(name, t);
			_loaded.rawset(name, t);
		}
		lib.postInstall(context, env, t);
	}

	@Override
	public LuaFunction _require() {
		return _require;
	}

	@Override
	public String _config() {
		return null;  // TODO
	}

	@Override
	public String _cpath() {
		return null;  // TODO
	}

	@Override
	public Table _loaded() {
		return _loaded;
	}

	@Override
	public LuaFunction _loadlib() {
		return _loadlib;
	}

	@Override
	public String _path() {
		return null;  // TODO
	}

	@Override
	public Table _preload() {
		return null;  // TODO
	}

	@Override
	public Table _searchers() {
		return null;  // TODO
	}

	@Override
	public LuaFunction _searchpath() {
		return _searchpath;
	}

	public class Require extends AbstractLibFunction {

		@Override
		protected String name() {
			return "require";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			String modName = args.nextString();

			Object mod = _loaded.rawget(modName);

			if (mod != null) {
				context.getReturnBuffer().setTo(mod);
			}
			else {
				throw new UnsupportedOperationException("loading module '" + modName + "': not implemented");
			}
		}

	}

}

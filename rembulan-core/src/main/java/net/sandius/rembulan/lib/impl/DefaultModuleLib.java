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
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.Lib;
import net.sandius.rembulan.lib.ModuleLib;
import net.sandius.rembulan.util.Check;

public class DefaultModuleLib extends ModuleLib {

	private final LuaState state;
	private final Table env;

	private final Table _loaded;

	private final Function _require;
	private final Function _loadlib;
	private final Function _searchpath;

	public DefaultModuleLib(LuaState state, Table env) {
		this.state = Check.notNull(state);
		this.env = Check.notNull(env);

		this._loaded = state.newTable();

		this._require = new Require();

		this._loadlib = new UnimplementedFunction("package.loadlib");
		this._searchpath = new UnimplementedFunction("package.searchpath");
	}

	@Override
	public void postInstall(LuaState state, Table env, Table libTable) {
		_loaded.rawset(name(), libTable);
	}

	@Override
	public void install(Lib lib) {
		lib.preInstall(state, env);
		Table t = lib.toTable(state.tableFactory());
		if (t != null) {
			String name = lib.name();
			env.rawset(name, t);
			_loaded.rawset(name, t);
		}
		lib.postInstall(state, env, t);
	}

	@Override
	public Function _require() {
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
	public Function _loadlib() {
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
	public Function _searchpath() {
		return _searchpath;
	}

	public class Require extends AbstractLibFunction {

		@Override
		protected String name() {
			return "require";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ControlThrowable {
			String modName = args.nextString();

			Object mod = _loaded.rawget(modName);

			if (mod != null) {
				context.getReturnVector().setTo(mod);
			}
			else {
				throw new UnsupportedOperationException("loading module '" + modName + "': not implemented");
			}
		}

	}

}

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

import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.LuaRuntimeException;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.Lib;
import net.sandius.rembulan.lib.ModuleLib;
import net.sandius.rembulan.runtime.Dispatch;
import net.sandius.rembulan.runtime.ExecutionContext;
import net.sandius.rembulan.runtime.LuaFunction;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.sandius.rembulan.runtime.UnresolvedControlThrowable;

import java.util.Objects;

public class DefaultModuleLib extends ModuleLib {

	private final StateContext context;
	private final Table env;

	private final Table _loaded;
	private final Table _preload;
	private final Table _searchers;

	private final LuaFunction _require;
	private final LuaFunction _loadlib;
	private final LuaFunction _searchpath;

	public DefaultModuleLib(StateContext context, Table env) {
		this.context = Objects.requireNonNull(context);
		this.env = Objects.requireNonNull(env);

		this._loaded = context.newTable();
		this._preload = context.newTable();
		this._searchers = context.newTable();

		// initialise searchers
		_searchers.rawset(1, new PreloadSearcher(_preload));

		this._require = new Require(env, _loaded);

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
		return _preload;
	}

	@Override
	public Table _searchers() {
		return _searchers;
	}

	@Override
	public LuaFunction _searchpath() {
		return _searchpath;
	}

	public static class Require extends AbstractLibFunction {

		private final Table env;
		private final Table loaded;

		public Require(Table env, Table loaded) {
			this.env = Objects.requireNonNull(env);
			this.loaded = Objects.requireNonNull(loaded);
		}

		private class SuspendedState {

			private final int state;
			private final String error;
			private final String modName;
			private final Table searchers;
			private final long idx;

			private SuspendedState(int state, String error, String modName, Table searchers, long idx) {
				this.state = state;
				this.error = error;
				this.modName = modName;
				this.searchers = searchers;
				this.idx = idx;
			}

		}

		@Override
		protected String name() {
			return "require";
		}

		private Table getSearchers() {
			Object pkg = env.rawget("package");
			if (pkg instanceof Table) {
				Object o = ((Table) pkg).rawget("searchers");
				if (o instanceof Table) {
					return (Table) o;
				}
			}
			throw new IllegalStateException("'package.searchers' must be a table");
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			String modName = args.nextString();

			Object mod = loaded.rawget(modName);

			if (mod != null) {
				// already loaded
				context.getReturnBuffer().setTo(mod);
			}
			else {
				// get package.searchers
				Table searchers = getSearchers();
				search(context, 0, "", modName, searchers, 1);
			}
		}

		private void search(ExecutionContext context, int state, String error, String modName, Table searchers, long idx)
				throws ResolvedControlThrowable {

			final LuaFunction loader;
			final Object origin;

			loop:
			while (true) {
				try {
					switch (state) {
						case 0:
							Object o = searchers.rawget(idx++);
							if (o == null) {
								// reached the end of the list
								throw new LuaRuntimeException("module '" + modName + "' not found:" + error);
							}
							state = 1;
							Dispatch.call(context, o, modName);

						case 1:
							Object result = context.getReturnBuffer().get0();
							if (result instanceof LuaFunction) {
								// found it
								loader = (LuaFunction) result;
								origin = context.getReturnBuffer().get1();

								break loop;
							}
							else {
								// not a loader

								// append error string
								String s = Conversions.stringValueOf(result);
								if (s != null) {
									error += s;
								}

								state = 0;  // continue with the next iteration
								break;
							}

						default:
							throw new IllegalStateException("Invalid state: " + state);
					}
				}
				catch (UnresolvedControlThrowable ct) {
					throw ct.resolve(this, new SuspendedState(state, error, modName, searchers, idx));
				}
			}

			load(context, modName, loader, origin);
		}

		private void load(ExecutionContext context, String modName, LuaFunction loader, Object origin)
				throws ResolvedControlThrowable {

			try {
				Dispatch.call(context, loader, modName, origin);
			}
			catch (UnresolvedControlThrowable ct) {
				throw ct.resolve(this, modName);
			}

			resumeLoad(context, modName);
		}

		private void resumeLoad(ExecutionContext context, String modName) {
			Object loadResult = context.getReturnBuffer().get0();
			Object requireResult = loadResult != null ? loadResult : true;

			loaded.rawset(modName, requireResult);
			context.getReturnBuffer().setTo(requireResult);
		}

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ResolvedControlThrowable {
			if (suspendedState instanceof SuspendedState) {
				SuspendedState ss = (SuspendedState) suspendedState;
				search(context, ss.state, ss.error, ss.modName, ss.searchers, ss.idx);
			}
			else {
				resumeLoad(context, (String) suspendedState);
			}
		}

	}

	static class PreloadSearcher extends AbstractLibFunction {

		private final Table preload;

		PreloadSearcher(Table preload) {
			this.preload = Objects.requireNonNull(preload);
		}

		@Override
		protected String name() {
			return "(preload searcher)";
		}

		@Override
		protected void invoke(ExecutionContext context, ArgumentIterator args) throws ResolvedControlThrowable {
			String modName = args.nextString();

			Object entry = preload.rawget(modName);

			if (entry != null) {
				context.getReturnBuffer().setTo(entry);
			}
			else {
				String error = "\n\tno field package.preload['" + modName + "']";
				context.getReturnBuffer().setTo(error);
			}
		}

	}

}

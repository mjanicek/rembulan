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

import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.ModuleLib;

public class DefaultModuleLib extends ModuleLib {

	private final Function _require;
	private final Function _loadlib;
	private final Function _searchpath;

	public DefaultModuleLib() {
		this._require = new UnimplementedFunction("require");
		this._loadlib = new UnimplementedFunction("package.loadlib");
		this._searchpath = new UnimplementedFunction("package.searchpath");
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
		return null;  // TODO
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

}

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

import net.sandius.rembulan.Function;
import net.sandius.rembulan.core.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.OsLib;

public class DefaultOsLib extends OsLib {

	private final Function _clock;
	private final Function _date;
	private final Function _difftime;
	private final Function _execute;
	private final Function _exit;
	private final Function _getenv;
	private final Function _remove;
	private final Function _rename;
	private final Function _setlocale;
	private final Function _time;
	private final Function _tmpname;
	
	public DefaultOsLib() {
		this._clock = new UnimplementedFunction("os.clock");  // TODO
		this._date = new UnimplementedFunction("os.date");  // TODO
		this._difftime = new UnimplementedFunction("os.difftime");  // TODO
		this._execute = new UnimplementedFunction("os.execute");  // TODO
		this._exit = new UnimplementedFunction("os.exit");  // TODO
		this._getenv = new UnimplementedFunction("os.getenv");  // TODO
		this._remove = new UnimplementedFunction("os.remove");  // TODO
		this._rename = new UnimplementedFunction("os.rename");  // TODO
		this._setlocale = new UnimplementedFunction("os.setlocale");  // TODO
		this._time = new UnimplementedFunction("os.time");  // TODO
		this._tmpname = new UnimplementedFunction("os.tmpname");  // TODO
	}
	
	@Override
	public Function _clock() {
		return _clock;
	}

	@Override
	public Function _date() {
		return _date;
	}

	@Override
	public Function _difftime() {
		return _difftime;
	}

	@Override
	public Function _execute() {
		return _execute;
	}

	@Override
	public Function _exit() {
		return _exit;
	}

	@Override
	public Function _getenv() {
		return _getenv;
	}

	@Override
	public Function _remove() {
		return _remove;
	}

	@Override
	public Function _rename() {
		return _rename;
	}

	@Override
	public Function _setlocale() {
		return _setlocale;
	}

	@Override
	public Function _time() {
		return _time;
	}

	@Override
	public Function _tmpname() {
		return _tmpname;
	}
	
}

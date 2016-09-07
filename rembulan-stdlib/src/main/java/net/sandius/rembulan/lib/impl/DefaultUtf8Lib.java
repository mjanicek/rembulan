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

import net.sandius.rembulan.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.Utf8Lib;
import net.sandius.rembulan.runtime.LuaFunction;

public class DefaultUtf8Lib extends Utf8Lib {

	private final LuaFunction _char;
	private final LuaFunction _codes;
	private final LuaFunction _codepoint;
	private final LuaFunction _len;
	private final LuaFunction _offset;
	
	public DefaultUtf8Lib() {
		this._char = new UnimplementedFunction("utf8.char");  // TODO
		this._codes = new UnimplementedFunction("utf8.codes");  // TODO
		this._codepoint = new UnimplementedFunction("utf8.codepoint");  // TODO
		this._len = new UnimplementedFunction("utf8.len");  // TODO
		this._offset = new UnimplementedFunction("utf8.offset");  // TODO
	}

	@Override
	public LuaFunction _char() {
		return _char;
	}

	@Override
	public String _charpattern() {
		return null;  // TODO
	}

	@Override
	public LuaFunction _codes() {
		return _codes;
	}

	@Override
	public LuaFunction _codepoint() {
		return _codepoint;
	}

	@Override
	public LuaFunction _len() {
		return _len;
	}

	@Override
	public LuaFunction _offset() {
		return _offset;
	}

}

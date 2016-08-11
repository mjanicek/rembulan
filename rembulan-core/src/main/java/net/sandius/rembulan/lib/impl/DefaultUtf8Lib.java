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
import net.sandius.rembulan.core.impl.UnimplementedFunction;
import net.sandius.rembulan.lib.Utf8Lib;

public class DefaultUtf8Lib extends Utf8Lib {

	private final Function _char;
	private final Function _codes;
	private final Function _codepoint;
	private final Function _len;
	private final Function _offset;
	
	public DefaultUtf8Lib() {
		this._char = new UnimplementedFunction("utf8.char");  // TODO
		this._codes = new UnimplementedFunction("utf8.codes");  // TODO
		this._codepoint = new UnimplementedFunction("utf8.codepoint");  // TODO
		this._len = new UnimplementedFunction("utf8.len");  // TODO
		this._offset = new UnimplementedFunction("utf8.offset");  // TODO
	}

	@Override
	public Function _char() {
		return _char;
	}

	@Override
	public String _charpattern() {
		return null;  // TODO
	}

	@Override
	public Function _codes() {
		return _codes;
	}

	@Override
	public Function _codepoint() {
		return _codepoint;
	}

	@Override
	public Function _len() {
		return _len;
	}

	@Override
	public Function _offset() {
		return _offset;
	}

}

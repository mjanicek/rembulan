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

package net.sandius.rembulan.core;

public abstract class Table extends LuaObject {

	public abstract Object rawget(Object key);

	public Object rawget(int idx) {
		return rawget((long) idx);
	}

	// must throw an exception when key is nil or NaN
	// must convert floats to ints if possible
	public abstract void rawset(Object key, Object value);

	public void rawset(int idx, Object value) {
		rawset((long) idx, value);
	}

	public abstract int rawlen();

	public abstract Object initialIndex();

	public abstract Object nextIndex(Object key);

}

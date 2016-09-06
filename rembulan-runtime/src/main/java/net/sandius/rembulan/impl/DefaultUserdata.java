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

package net.sandius.rembulan.impl;

import net.sandius.rembulan.Table;
import net.sandius.rembulan.Userdata;

/**
 * Default implementation of full userdata.
 */
public abstract class DefaultUserdata extends Userdata {

	private Table mt;
	private Object userValue;

	/**
	 * Constructs a new instance of this userdata with the specified initial {@code metatable}
	 * and {@code userValue}.
	 *
	 * @param metatable  initial metatable, may be {@code null}
	 * @param userValue  initial user value, may be {@code null}
	 */
	public DefaultUserdata(Table metatable, Object userValue) {
		this.mt = metatable;
		this.userValue = userValue;
	}

	@Override
	public Table getMetatable() {
		return mt;
	}

	@Override
	public Table setMetatable(Table mt) {
		Table old = this.mt;
		this.mt = mt;
		return old;
	}

	@Override
	public Object getUserValue() {
		return userValue;
	}

	@Override
	public Object setUserValue(Object value) {
		Object oldValue = userValue;
		this.userValue = value;
		return oldValue;
	}

}

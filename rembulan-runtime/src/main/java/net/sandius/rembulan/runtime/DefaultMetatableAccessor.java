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

package net.sandius.rembulan.runtime;

import net.sandius.rembulan.LuaObject;
import net.sandius.rembulan.LuaType;
import net.sandius.rembulan.MetatableAccessor;
import net.sandius.rembulan.Table;

class DefaultMetatableAccessor implements MetatableAccessor {

	/**
	 * The {@code nil} metatable.
	 */
	protected Table nilMetatable;

	/**
	 * The {@code boolean} metatable.
	 */
	protected Table booleanMetatable;

	/**
	 * The {@code number} metatable.
	 */
	protected Table numberMetatable;

	/**
	 * The {@code string} metatable.
	 */
	protected Table stringMetatable;

	/**
	 * The {@code function} metatable.
	 */
	protected Table functionMetatable;

	/**
	 * The {@code thread} metatable.
	 */
	protected Table threadMetatable;

	/**
	 * The light userdata metatable.
	 */
	protected Table lightuserdataMetatable;

	@Override
	public Table getNilMetatable() {
		return nilMetatable;
	}

	@Override
	public Table getBooleanMetatable() {
		return booleanMetatable;
	}

	@Override
	public Table getNumberMetatable() {
		return numberMetatable;
	}

	@Override
	public Table getStringMetatable() {
		return stringMetatable;
	}

	@Override
	public Table getFunctionMetatable() {
		return functionMetatable;
	}

	@Override
	public Table getThreadMetatable() {
		return threadMetatable;
	}

	@Override
	public Table getLightUserdataMetatable() {
		return lightuserdataMetatable;
	}

	@Override
	public Table getMetatable(Object o) {
		if (o instanceof LuaObject) {
			return ((LuaObject) o).getMetatable();
		}
		else {
			LuaType type = LuaType.typeOf(o);
			switch (type) {
				case NIL: return getNilMetatable();
				case BOOLEAN: return getBooleanMetatable();
				case NUMBER: return getNumberMetatable();
				case STRING: return getStringMetatable();
				case FUNCTION: return getFunctionMetatable();
				case THREAD: return getThreadMetatable();
				case USERDATA: return getLightUserdataMetatable();
				default: throw new IllegalStateException("Illegal type: " + type);
			}
		}
	}

	@Override
	public Table setNilMetatable(Table table) {
		Table old = nilMetatable;
		nilMetatable = table;
		return old;
	}

	@Override
	public Table setBooleanMetatable(Table table) {
		Table old = booleanMetatable;
		booleanMetatable = table;
		return old;
	}

	@Override
	public Table setNumberMetatable(Table table) {
		Table old = numberMetatable;
		numberMetatable = table;
		return old;
	}

	@Override
	public Table setStringMetatable(Table table) {
		Table old = stringMetatable;
		stringMetatable = table;
		return old;
	}

	@Override
	public Table setFunctionMetatable(Table table) {
		Table old = functionMetatable;
		functionMetatable = table;
		return old;
	}

	@Override
	public Table setThreadMetatable(Table table) {
		Table old = threadMetatable;
		threadMetatable = table;
		return old;
	}

	@Override
	public Table setLightUserdataMetatable(Table table) {
		Table old = lightuserdataMetatable;
		lightuserdataMetatable = table;
		return old;
	}

	@Override
	public Table setMetatable(Object o, Table table) {
		if (o instanceof LuaObject) {
			return ((LuaObject) o).setMetatable(table);
		}
		else {
			LuaType type = LuaType.typeOf(o);
			switch (type) {
				case NIL: return setNilMetatable(table);
				case BOOLEAN: return setBooleanMetatable(table);
				case NUMBER: return setNumberMetatable(table);
				case STRING: return setStringMetatable(table);
				case FUNCTION: return setFunctionMetatable(table);
				case THREAD: return setThreadMetatable(table);
				case USERDATA: return setLightUserdataMetatable(table);
				default: throw new IllegalStateException("Illegal type: " + type);
			}
		}
	}

}

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

import net.sandius.rembulan.LuaState;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.TableFactory;
import net.sandius.rembulan.exec.CallInitialiser;
import net.sandius.rembulan.exec.OneShotContinuation;
import net.sandius.rembulan.impl.DefaultTable;
import net.sandius.rembulan.util.Check;

/**
 * Default implementation of Lua states that is also a call initialiser.
 */
public class DefaultLuaState extends LuaState implements CallInitialiser {

	private final TableFactory tableFactory;

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

	/**
	 * Constructs a new {@code DefaultLuaState} with the specified table factory
	 * {@code tableFactory}.
	 *
	 * @param tableFactory  table factory to be used by this state, must not be {@code null}
	 *
	 * @throws IllegalArgumentException  if {@code tableFactory} is {@code null}
	 */
	public DefaultLuaState(TableFactory tableFactory) {
		this.tableFactory = Check.notNull(tableFactory);
	}

	/**
	 * Constructs a new {@code DefaultLuaState} with the default table factory.
	 */
	public DefaultLuaState() {
		this(DefaultTable.factory());
	}

	@Override
	public Table nilMetatable() {
		return nilMetatable;
	}

	@Override
	public Table booleanMetatable() {
		return booleanMetatable;
	}

	@Override
	public Table numberMetatable() {
		return numberMetatable;
	}

	@Override
	public Table stringMetatable() {
		return stringMetatable;
	}

	@Override
	public Table functionMetatable() {
		return functionMetatable;
	}

	@Override
	public Table threadMetatable() {
		return threadMetatable;
	}

	@Override
	public Table lightUserdataMetatable() {
		return lightuserdataMetatable;
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
	public TableFactory tableFactory() {
		return tableFactory;
	}

	@Override
	public OneShotContinuation newCall(Object fn, Object... args) {
		return Call.init(this, fn, args).getCurrentContinuation();
	}

}

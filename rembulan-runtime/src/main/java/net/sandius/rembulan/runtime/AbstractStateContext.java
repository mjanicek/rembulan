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

import net.sandius.rembulan.MetatableAccessor;
import net.sandius.rembulan.StateContext;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.TableFactory;

abstract class AbstractStateContext implements StateContext {

	private final TableFactory tableFactory;
	private final MetatableAccessor metatableAccessor;

	AbstractStateContext(TableFactory tableFactory, MetatableAccessor metatableAccessor) {
		this.tableFactory = tableFactory;
		this.metatableAccessor = metatableAccessor;
	}

	AbstractStateContext(StateContext stateContext) {
		this(stateContext, stateContext);
	}

	@Override
	public Table getMetatable(Object instance) {
		return metatableAccessor.getMetatable(instance);
	}

	@Override
	public Table nilMetatable() {
		return metatableAccessor.nilMetatable();
	}

	@Override
	public Table booleanMetatable() {
		return metatableAccessor.booleanMetatable();
	}

	@Override
	public Table numberMetatable() {
		return metatableAccessor.numberMetatable();
	}

	@Override
	public Table stringMetatable() {
		return metatableAccessor.stringMetatable();
	}

	@Override
	public Table functionMetatable() {
		return metatableAccessor.functionMetatable();
	}

	@Override
	public Table threadMetatable() {
		return metatableAccessor.threadMetatable();
	}

	@Override
	public Table lightUserdataMetatable() {
		return metatableAccessor.lightUserdataMetatable();
	}

	@Override
	public Table setMetatable(Object instance, Table table) {
		return metatableAccessor.setMetatable(instance, table);
	}

	@Override
	public Table setNilMetatable(Table table) {
		return metatableAccessor.setNilMetatable(table);
	}

	@Override
	public Table setBooleanMetatable(Table table) {
		return metatableAccessor.setBooleanMetatable(table);
	}

	@Override
	public Table setNumberMetatable(Table table) {
		return metatableAccessor.setNumberMetatable(table);
	}

	@Override
	public Table setStringMetatable(Table table) {
		return metatableAccessor.setStringMetatable(table);
	}

	@Override
	public Table setFunctionMetatable(Table table) {
		return metatableAccessor.setFunctionMetatable(table);
	}

	@Override
	public Table setThreadMetatable(Table table) {
		return metatableAccessor.setThreadMetatable(table);
	}

	@Override
	public Table setLightUserdataMetatable(Table table) {
		return metatableAccessor.setLightUserdataMetatable(table);
	}

	@Override
	public Table newTable() {
		return tableFactory.newTable();
	}

	@Override
	public Table newTable(int array, int hash) {
		return tableFactory.newTable(array, hash);
	}


}

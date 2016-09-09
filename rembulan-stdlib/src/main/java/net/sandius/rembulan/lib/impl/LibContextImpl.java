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

import net.sandius.rembulan.MetatableAccessor;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.TableFactory;
import net.sandius.rembulan.lib.LibContext;

import java.util.Objects;

class LibContextImpl implements LibContext {

	private final TableFactory tableFactory;
	private final MetatableAccessor metatableAccessor;

	public LibContextImpl(TableFactory tableFactory, MetatableAccessor metatableAccessor) {
		this.tableFactory = Objects.requireNonNull(tableFactory);
		this.metatableAccessor = Objects.requireNonNull(metatableAccessor);
	}

	@Override
	public Table newTable() {
		return tableFactory.newTable();
	}

	@Override
	public Table newTable(int array, int hash) {
		return tableFactory.newTable(array, hash);
	}

	@Override
	public Table getMetatable(Object instance) {
		return metatableAccessor.getMetatable(instance);
	}

	@Override
	public Table setMetatable(Object instance, Table table) {
		return metatableAccessor.setMetatable(instance, table);
	}

}

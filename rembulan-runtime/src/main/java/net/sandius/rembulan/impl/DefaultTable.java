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

import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.TableFactory;
import net.sandius.rembulan.util.TraversableHashMap;

import java.util.NoSuchElementException;

/**
 * Default implementation of the Lua table storing all key-value pairs in a hashmap.
 * The table implementation does not support weak keys or values.
 */
public class DefaultTable extends Table {

	private final TraversableHashMap<Object, Object> values;

	public DefaultTable() {
		this.values = new TraversableHashMap<>();
	}

	static class Factory implements TableFactory {
		@Override
		public Table newTable() {
			return newTable(0, 0);
		}

		@Override
		public Table newTable(int array, int hash) {
			return new DefaultTable();
		}
	}

	private static final TableFactory FACTORY_INSTANCE = new Factory();

	/**
	 * Returns the table factory for constructing instances of {@code DefaultTable}.
	 *
	 * @return  the table factory for {@code DefaultTable}s
	 */
	public static TableFactory factory() {
		return FACTORY_INSTANCE;
	}

	@Override
	public Object rawget(Object key) {
		key = Conversions.normaliseKey(key);
		return key != null ? values.get(key) : null;
	}

	@Override
	public void rawset(Object key, Object value) {
		key = Conversions.normaliseKey(key);

		if (key == null) {
			throw new IllegalArgumentException("table index is nil");
		}
		if (key instanceof Double && Double.isNaN(((Double) key).doubleValue())) {
			throw new IllegalArgumentException("table index is NaN");
		}

		value = Conversions.canonicalRepresentationOf(value);

		if (value == null) {
			values.remove(key);
		}
		else {
			values.put(key, value);
		}

		updateBasetableModes(key, value);
	}

	@Override
	public Object initialKey() {
		return values.getFirstKey();
	}

	@Override
	public Object successorKeyOf(Object key) {
		try {
			return values.getSuccessorOf(key);
		}
		catch (NoSuchElementException | NullPointerException ex) {
			throw new IllegalArgumentException("invalid key to 'next'", ex);
		}
	}

	@Override
	protected void setMode(boolean weakKeys, boolean weakValues) {
		// TODO
	}

}

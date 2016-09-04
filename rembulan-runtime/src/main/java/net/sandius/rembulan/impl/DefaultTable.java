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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DefaultTable extends Table {

	private Table mt;
	private final Map<Object, Object> values;

	public DefaultTable() {
		this.values = new HashMap<Object, Object>();
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

	public static final TableFactory FACTORY_INSTANCE = new Factory();

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

		if (value == null) {
			values.remove(key);
		}
		else {
			values.put(key, value);
		}
	}

	@Override
	public int rawlen() {
		int idx = 1;

		while (rawget(idx) != null) {
			++idx;
		}

		return idx - 1;
	}

	private Object next(Iterator<Object> it) {
		return it.hasNext() ? it.next() : null;
	}

	@Override
	public Object initialKey() {
		Iterator<Object> it = values.keySet().iterator();
		return next(it);
	}

	@Override
	public Object successorKeyOf(Object key) {
		// FIXME: extremely inefficient!
		Iterator<Object> it = values.keySet().iterator();

		while (it.hasNext()) {
			Object k = it.next();
			if (k.equals(key)) {
				return next(it);
			}
		}

		throw new IllegalArgumentException("invalid key to 'next'");
	}

}

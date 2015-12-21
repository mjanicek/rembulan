package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.TableFactory;

import java.util.HashMap;
import java.util.Map;

public class DefaultTable extends Table {

	private Table mt;
	private final Map<Object, Object> values;

	public DefaultTable() {
		this.values = new HashMap<Object, Object>();
	}

	public static final TableFactory FACTORY_INSTANCE = new TableFactory() {
		@Override
		public Table newTable() {
			return new DefaultTable();
		}
	};

	@Override
	public Table getMetatable() {
		return mt;
	}

	public void setMetatable(Table mt) {
		this.mt = mt;
	}

	@Override
	public Object rawget(Object key) {
		return key != null ? values.get(key) : null;
	}

	@Override
	public void rawset(Object key, Object value) {
		if (key == null) {
			throw new IllegalArgumentException("table index is nil");
		}
		if (Conversions.isNaN(key)) {
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

}
package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Userdata;
import net.sandius.rembulan.util.Check;

public class DefaultUserdata extends Userdata {

	private Table mt;
	private final Object data;

	public DefaultUserdata(Object data) {
		Check.notNull(data);
		this.data = data;
	}

	@Override
	public Table getMetatable() {
		return mt;
	}

	@Override
	public void setMetatable(Table mt) {
		this.mt = mt;
	}

	@Override
	public Object getData() {
		return data;
	}

}

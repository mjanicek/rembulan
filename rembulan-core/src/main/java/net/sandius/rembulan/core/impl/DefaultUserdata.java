package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Userdata;

public abstract class DefaultUserdata<T> extends Userdata {

	private Table mt;

	public DefaultUserdata(Table metatable) {
		this.mt = metatable;
	}

	@Override
	public Table getMetatable() {
		return mt;
	}

	@Override
	public void setMetatable(Table mt) {
		this.mt = mt;
	}

}

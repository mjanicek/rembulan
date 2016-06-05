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
	public Table setMetatable(Table mt) {
		Table old = this.mt;
		this.mt = mt;
		return old;
	}

}

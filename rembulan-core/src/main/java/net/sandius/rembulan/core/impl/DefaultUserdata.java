package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Userdata;

public abstract class DefaultUserdata extends Userdata {

	private Table mt;
	private Object userValue;

	public DefaultUserdata(Table metatable, Object userValue) {
		this.mt = metatable;
		this.userValue = userValue;
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

	@Override
	public Object getUserValue() {
		return userValue;
	}

	@Override
	public Object setUserValue(Object value) {
		Object oldValue = userValue;
		this.userValue = value;
		return oldValue;
	}

}

package net.sandius.rembulan.core;

public abstract class Table extends LuaObject {

	@Override
	public String toString() {
		return "table: 0x" + Integer.toHexString(hashCode());
	}

	public abstract Object rawget(Object key);

	public Object rawget(int idx) {
		return rawget((long) idx);
	}

	public abstract void rawset(Object key, Object value);

	public void rawset(int idx, Object value) {
		rawset((long) idx, value);
	}

	public abstract int rawlen();

}

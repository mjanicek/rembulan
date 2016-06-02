package net.sandius.rembulan.core;

public abstract class Table extends LuaObject {

	@Override
	public String toString() {
		return "table: 0x" + Integer.toHexString(hashCode());
	}

	public abstract Object rawget(Object key);

	public Object rawget(int idx) {
		return rawget(LNumber.valueOf(idx));
	}

	// must throw an exception when key is nil or NaN
	public abstract void rawset(Object key, Object value);

	public void rawset(int idx, Object value) {
		rawset(LNumber.valueOf(idx), value);
	}

	public abstract int rawlen();

	public abstract Object initialIndex();

	public abstract Object nextIndex(Object key);

}

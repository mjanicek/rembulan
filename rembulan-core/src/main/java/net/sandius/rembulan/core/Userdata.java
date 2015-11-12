package net.sandius.rembulan.core;

// full userdata
public abstract class Userdata extends LuaObject {

	@Override
	public String toString() {
		return "userdata: 0x" + Integer.toHexString(hashCode());
	}

	public abstract Object getData();

}

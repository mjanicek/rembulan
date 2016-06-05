package net.sandius.rembulan.core;

// full userdata
public abstract class Userdata extends LuaObject {

	public abstract Object getUserValue();

	public abstract Object setUserValue(Object value);

}

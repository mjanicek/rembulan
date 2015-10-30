package net.sandius.rembulan.core;

public abstract class LuaObject {

	public abstract Table getMetatable();

	public abstract void setMetatable(Table mt);

}

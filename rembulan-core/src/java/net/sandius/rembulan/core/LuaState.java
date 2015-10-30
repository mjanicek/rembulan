package net.sandius.rembulan.core;

public abstract class LuaState {

	public abstract Table nilMetatable();
	public abstract Table booleanMetatable();
	public abstract Table numberMetatable();
	public abstract Table stringMetatable();
	public abstract Table functionMetatable();
	public abstract Table threadMetatable();
	public abstract Table lightuserdataMetatable();

	public static LuaState getCurrent() {
		throw new UnsupportedOperationException();  // TODO
	}

}

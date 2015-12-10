package net.sandius.rembulan.core;

public abstract class LuaState {

	@Deprecated
	private static final ThreadLocal<LuaState> current = new ThreadLocal<LuaState>();

	@Deprecated
	public static LuaState getCurrentState() {
		return current.get();
	}

	@Deprecated
	public static void setCurrentState(LuaState state) {
		current.set(state);
	}

	@Deprecated
	public static void unsetCurrentState() {
		current.remove();
	}

	public abstract Table nilMetatable();
	public abstract Table booleanMetatable();
	public abstract Table numberMetatable();
	public abstract Table stringMetatable();
	public abstract Table functionMetatable();
	public abstract Table threadMetatable();
	public abstract Table lightuserdataMetatable();

	public abstract boolean shouldPreemptNow();

	@Deprecated
	public abstract Coroutine getCurrentCoroutine();

}

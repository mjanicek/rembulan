package net.sandius.rembulan.core;

public interface CoroutineFactory {

	Coroutine newCoroutine(LuaState state);

}

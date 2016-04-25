package net.sandius.rembulan.core;

public interface ExecutionContext {

	LuaState getState();

	ObjectSink getObjectSink();

	Coroutine getCurrentCoroutine();

	Coroutine newCoroutine(Function function);

	boolean canYield();

}

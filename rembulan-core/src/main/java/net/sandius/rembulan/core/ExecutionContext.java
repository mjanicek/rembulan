package net.sandius.rembulan.core;

public interface ExecutionContext {

	LuaState getState();

	ObjectSink getObjectSink();

	Coroutine getCurrentCoroutine();

}

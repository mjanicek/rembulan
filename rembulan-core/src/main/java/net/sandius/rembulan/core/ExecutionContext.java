package net.sandius.rembulan.core;

public interface ExecutionContext {

	LuaState getState();

	ObjectSink getObjectSink();

	Coroutine getCurrentCoroutine();

	Coroutine newCoroutine(Function function);

	boolean canYield();

	void resume(Coroutine coroutine, Object[] args) throws ControlThrowable;

	void yield(Object[] args) throws ControlThrowable;

	void checkPreempt(int cost) throws ControlThrowable;

}

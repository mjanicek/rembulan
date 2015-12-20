package net.sandius.rembulan.core;

public interface Resumable {

	void resume(LuaState state, ObjectSink result, Object suspendedState) throws ControlThrowable;

}

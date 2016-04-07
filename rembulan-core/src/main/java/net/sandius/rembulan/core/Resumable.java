package net.sandius.rembulan.core;

import java.io.Serializable;

public interface Resumable {

	void resume(LuaState state, ObjectSink result, Serializable suspendedState) throws ControlThrowable;

}

package net.sandius.rembulan.core;

import java.io.Serializable;

public interface Resumable {

	void resume(ExecutionContext context, Serializable suspendedState) throws ControlThrowable;

}

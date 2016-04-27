package net.sandius.rembulan.core;

public interface Resumable {

	void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable;

}

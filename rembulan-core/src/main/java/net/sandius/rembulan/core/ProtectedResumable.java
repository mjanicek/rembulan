package net.sandius.rembulan.core;

public interface ProtectedResumable extends Resumable {

	void resumeError(ExecutionContext context, Object suspendedState, Object error) throws ControlThrowable;

}

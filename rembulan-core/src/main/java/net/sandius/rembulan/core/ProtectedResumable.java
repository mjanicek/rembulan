package net.sandius.rembulan.core;

public interface ProtectedResumable extends Resumable {

	Preemption resumeError(ExecutionContext context, Object suspendedState, Object error);

}

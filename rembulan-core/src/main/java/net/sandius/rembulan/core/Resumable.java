package net.sandius.rembulan.core;

public interface Resumable {

	Preemption resume(ExecutionContext context, Object suspendedState);

}

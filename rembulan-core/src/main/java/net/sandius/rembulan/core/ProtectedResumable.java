package net.sandius.rembulan.core;

import java.io.Serializable;

public interface ProtectedResumable extends Resumable {

	void resumeError(ExecutionContext context, Serializable suspendedState, Object error);

}

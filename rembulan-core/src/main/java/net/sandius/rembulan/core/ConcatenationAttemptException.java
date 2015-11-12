package net.sandius.rembulan.core;

public class ConcatenationAttemptException extends IllegalOperationAttemptException {

	public ConcatenationAttemptException(String target) {
		super("concatenate", target);
	}

}

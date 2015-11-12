package net.sandius.rembulan.core;

public class IllegalOperationAttemptException extends RuntimeException {

	public IllegalOperationAttemptException(String message) {
		super("attempt to " + message);
	}

	public IllegalOperationAttemptException(String opName, String target) {
		super("attempt to " + opName + " a " + target + " value");
	}

}

package net.sandius.rembulan.core;

public class NoIntegerRepresentationException extends IllegalOperationAttemptException {

	public NoIntegerRepresentationException() {
		super("number has no integer representation");
	}

}

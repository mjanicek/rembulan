package net.sandius.rembulan.lib;

public class UnexpectedArgumentException extends IllegalArgumentException {

	public UnexpectedArgumentException(String message) {
		super(message);
	}

	public UnexpectedArgumentException(String expected, String actual) {
		this(expected + " expected, got " + actual);
	}

}

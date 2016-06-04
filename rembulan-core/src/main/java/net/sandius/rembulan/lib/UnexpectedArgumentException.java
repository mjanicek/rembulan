package net.sandius.rembulan.lib;

public class UnexpectedArgumentException extends IllegalArgumentException {

	public UnexpectedArgumentException(String expected, String actual) {
		super(expected + " expected, got " + actual);
	}

}

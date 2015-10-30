package net.sandius.rembulan.core;

public class NoIntegerRepresentationException extends ArithmeticException {

	public NoIntegerRepresentationException() {
		super("number has no integer representation");
	}

}

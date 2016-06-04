package net.sandius.rembulan.core;

public class NoIntegerRepresentationException extends ConversionException {

	public NoIntegerRepresentationException() {
		super("number has no integer representation");
	}

}

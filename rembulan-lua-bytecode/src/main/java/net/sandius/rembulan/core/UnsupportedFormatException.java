package net.sandius.rembulan.core;

public class UnsupportedFormatException extends UnsupportedOperationException {

	public UnsupportedFormatException(String message) {
		super(message);
	}

	public UnsupportedFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedFormatException(Throwable cause) {
		super(cause);
	}

}

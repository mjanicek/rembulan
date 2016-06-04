package net.sandius.rembulan.lib;

public class BadArgumentException extends IllegalArgumentException {

	public BadArgumentException(int argumentIndex, String functionName, String message) {
		super("bad argument #" + argumentIndex + " to '" + functionName + "' (" + message + ")");
	}

	public BadArgumentException(int argumentIndex, String functionName, Throwable cause) {
		this(argumentIndex, functionName, cause.getMessage());
	}

}

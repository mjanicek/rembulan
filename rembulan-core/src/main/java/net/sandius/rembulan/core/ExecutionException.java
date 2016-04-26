package net.sandius.rembulan.core;

// FIXME: find a better name (clashes with java.util.concurrent.ExecutionException), or perhaps use it?
public class ExecutionException extends RuntimeException {

	public ExecutionException(Throwable cause) {
		super(cause);
	}

}

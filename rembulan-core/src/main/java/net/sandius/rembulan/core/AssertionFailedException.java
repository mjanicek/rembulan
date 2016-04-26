package net.sandius.rembulan.core;

public class AssertionFailedException extends LuaRuntimeException {

	public AssertionFailedException(String message) {
		super(message);
	}

	public AssertionFailedException(Object errorObject) {
		super(errorObject);
	}

}

package net.sandius.rembulan.lib;

import net.sandius.rembulan.core.LuaRuntimeException;

public class AssertionFailedException extends LuaRuntimeException {

	public AssertionFailedException(String message) {
		super(message);
	}

	public AssertionFailedException(Object errorObject) {
		super(errorObject);
	}

}

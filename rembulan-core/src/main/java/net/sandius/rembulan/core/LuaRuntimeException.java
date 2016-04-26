package net.sandius.rembulan.core;

public class LuaRuntimeException extends RuntimeException {

	private final Object errorObject;

	public LuaRuntimeException(String message) {
		this.errorObject = message;
	}

	public LuaRuntimeException(Object errorObject) {
		this.errorObject = errorObject;
	}

	@Override
	public String getMessage() {
		return Conversions.objectToString(errorObject);
	}

	public Object getErrorObject() {
		return errorObject;
	}

}

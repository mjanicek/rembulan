package net.sandius.rembulan.core;

public class LuaRuntimeException extends RuntimeException {

	private final Object errorObject;

	public LuaRuntimeException(Object errorObject) {
		super();
		this.errorObject = errorObject;
	}

	@Override
	public String getMessage() {
		return Conversions.toHumanReadableString(errorObject);
	}

	public Object getErrorObject() {
		return errorObject;
	}

}

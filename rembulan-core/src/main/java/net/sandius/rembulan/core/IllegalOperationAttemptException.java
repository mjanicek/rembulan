package net.sandius.rembulan.core;

public class IllegalOperationAttemptException extends RuntimeException {

	public IllegalOperationAttemptException(String message) {
		super("attempt to " + message);
	}

	public IllegalOperationAttemptException(String opName, String target) {
		super("attempt to " + opName + " a " + target + " value");
	}

	public static IllegalOperationAttemptException arithmetic(Object a, Object b) {
		String typeName = Value.typeOf(Conversions.objectAsNumber(a) == null ? a : b).name;
		return new IllegalOperationAttemptException("perform arithmetic on", typeName);
	}

	public static IllegalOperationAttemptException comparison(Object a, Object b) {
		String ta = Value.typeOf(a).name;
		String tb = Value.typeOf(b).name;
		return new IllegalOperationAttemptException("compare " + ta + " with " + tb);
	}

	public static IllegalOperationAttemptException call(Object o) {
		return new IllegalOperationAttemptException("call", Value.typeOf(o).name);
	}

	public static IllegalOperationAttemptException index(Object o) {
		return new IllegalOperationAttemptException("index", Value.typeOf(o).name);
	}

}

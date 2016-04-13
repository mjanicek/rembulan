package net.sandius.rembulan.core;

public class IllegalOperationAttemptException extends RuntimeException {

	public IllegalOperationAttemptException(String message) {
		super(message);
	}

	public IllegalOperationAttemptException(String opName, String target) {
		this("attempt to " + opName + " a " + target + " value");
	}

	public static IllegalOperationAttemptException arithmetic(Object a, Object b) {
		String typeName = Value.typeOf(Conversions.objectAsNumber(a) == null ? a : b).name;
		return new IllegalOperationAttemptException("perform arithmetic on", typeName);
	}

	public static IllegalOperationAttemptException comparison(Object a, Object b) {
		String ta = Value.typeOf(a).name;
		String tb = Value.typeOf(b).name;
		return new IllegalOperationAttemptException("attempt to compare " + ta + " with " + tb);
	}

	public static IllegalOperationAttemptException call(Object o) {
		return new IllegalOperationAttemptException("call", Value.typeOf(o).name);
	}

	public static IllegalOperationAttemptException index(Object o) {
		return new IllegalOperationAttemptException("index", Value.typeOf(o).name);
	}

	public static IllegalOperationAttemptException bitwise(Object a, Object b) {
		Object nonNumeric = Conversions.objectAsNumber(a) == null ? a : b;

		if (Conversions.objectAsNumber(nonNumeric) == null) {
			// indeed it's not a number
			String typeName = Value.typeOf(nonNumeric).name;
			return new IllegalOperationAttemptException("perform bitwise operation on", typeName);
		}
		else {
			return new IllegalOperationAttemptException("number has no integer representation");
		}
	}

	public static IllegalOperationAttemptException bitwise(Object o) {
		if (Conversions.objectAsNumber(o) == null) {
			// indeed it's not a number
			String typeName = Value.typeOf(o).name;
			return new IllegalOperationAttemptException("perform bitwise operation on", typeName);
		}
		else {
			return new IllegalOperationAttemptException("number has no integer representation");
		}
	}

}

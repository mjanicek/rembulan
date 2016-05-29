package net.sandius.rembulan.core;

public class IllegalOperationAttemptException extends LuaRuntimeException {

	public IllegalOperationAttemptException(String message) {
		super(message);
	}

	public IllegalOperationAttemptException(String opName, String target) {
		this("attempt to " + opName + " a " + target + " value");
	}

	public static IllegalOperationAttemptException arithmetic(Object a, Object b) {
		String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(Conversions.objectAsNumber(a) == null ? a : b);
		return new IllegalOperationAttemptException("perform arithmetic on", typeName);
	}

	public static IllegalOperationAttemptException arithmetic(Object o) {
		String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(o);
		return new IllegalOperationAttemptException("perform arithmetic on", typeName);
	}

	public static IllegalOperationAttemptException comparison(Object a, Object b) {
		String ta = PlainValueTypeNamer.INSTANCE.typeNameOf(a);
		String tb = PlainValueTypeNamer.INSTANCE.typeNameOf(b);
		return new IllegalOperationAttemptException("attempt to compare " + ta + " with " + tb);
	}

	public static IllegalOperationAttemptException call(Object o) {
		return new IllegalOperationAttemptException("call", PlainValueTypeNamer.INSTANCE.typeNameOf(o));
	}

	public static IllegalOperationAttemptException index(Object o) {
		return new IllegalOperationAttemptException("index", PlainValueTypeNamer.INSTANCE.typeNameOf(o));
	}

	public static IllegalOperationAttemptException bitwise(Object a, Object b) {
		Object nonNumeric = Conversions.objectAsNumber(a) == null ? a : b;

		if (Conversions.objectAsNumber(nonNumeric) == null) {
			// indeed it's not a number
			String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(nonNumeric);
			return new IllegalOperationAttemptException("perform bitwise operation on", typeName);
		}
		else {
			return new IllegalOperationAttemptException("number has no integer representation");
		}
	}

	public static IllegalOperationAttemptException bitwise(Object o) {
		if (Conversions.objectAsNumber(o) == null) {
			// indeed it's not a number
			String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(o);
			return new IllegalOperationAttemptException("perform bitwise operation on", typeName);
		}
		else {
			return new IllegalOperationAttemptException("number has no integer representation");
		}
	}

	public static IllegalOperationAttemptException length(Object o) {
		String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(o);
		return new IllegalOperationAttemptException("attempt to get length of a " + typeName + " value");
	}

	public static IllegalOperationAttemptException concatenate(Object a, Object b) {
		String typeName = PlainValueTypeNamer.INSTANCE.typeNameOf(Conversions.objectAsString(a) == null ? a : b);
		return new IllegalOperationAttemptException("concatenate", typeName);
	}

}

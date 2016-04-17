package net.sandius.rembulan.core;

public class NonsuspendableFunctionException extends UnsupportedOperationException {

	public NonsuspendableFunctionException(Class<? extends Function> clazz) {
		super("Function is not suspendable: " + clazz.getName());
	}

}

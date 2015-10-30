package net.sandius.rembulan.core;

public class ArithmeticAttemptException extends IllegalOperationAttemptException {

	public ArithmeticAttemptException(String tpe) {
		super("perform arithmetic on", tpe);
	}

}

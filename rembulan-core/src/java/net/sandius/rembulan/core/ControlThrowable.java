package net.sandius.rembulan.core;

public abstract class ControlThrowable extends Throwable {

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

}

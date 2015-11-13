package net.sandius.rembulan.core;

public abstract class ControlThrowable extends Throwable {

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	public abstract void push(CallInfo ci);

}

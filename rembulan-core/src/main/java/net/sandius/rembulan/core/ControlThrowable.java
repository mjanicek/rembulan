package net.sandius.rembulan.core;

import java.util.Iterator;

public abstract class ControlThrowable extends Throwable {

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	public abstract void push(CallInfo ci);

	public abstract Iterator<CallInfo> frameIterator();

}

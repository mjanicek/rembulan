package net.sandius.rembulan.core;

import java.util.ListIterator;

public abstract class ControlThrowable extends Throwable {

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	public abstract void push(CallInfo ci);

	public void pushCall(Function function, int base, int ret, int pc, int numResults, int flags) {
		push(new CallInfo(function, base, ret, pc, numResults, flags));
	}

	// LIFO iterator
	public abstract ListIterator<CallInfo> frameIterator();

}

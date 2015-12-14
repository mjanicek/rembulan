package net.sandius.rembulan.core;

import java.util.Iterator;

public abstract class ControlThrowable extends Throwable {

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	public abstract void push(CallInfo ci);

	public void pushCall(Function function, int base, int ret, int pc, int numResults, int flags) {
		push(new CallInfo(function, base, ret, pc, numResults, flags));
	}

	public abstract Iterator<CallInfo> frameIterator();

	public abstract CallInfo[] frames();

	@Deprecated
	public CallInfo last() {
		Iterator<CallInfo> it = frameIterator();
		CallInfo result = null;
		while (it.hasNext()) {
			result = it.next();
		}
		return result;
	}

}

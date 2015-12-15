package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

public abstract class ControlThrowable extends Throwable {

	protected final ArrayList<CallInfo> callStack;

	protected ControlThrowable() {
		this.callStack = new ArrayList<>();
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	public void push(CallInfo ci) {
		Check.notNull(ci);
		callStack.add(ci);
	}

	public void pushCall(Function function, int base, int ret, int pc, int numResults, int flags) {
		push(new CallInfo(function, base, ret, pc, numResults, flags));
	}

	// LIFO iterator
	public ListIterator<CallInfo> frameIterator() {
		return Collections.unmodifiableList(callStack).listIterator();
	}

}

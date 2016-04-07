package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

public abstract class ControlThrowable extends Throwable {

	protected final LinkedList<CallInfo> callStack;

	protected ControlThrowable() {
		this.callStack = new LinkedList<>();
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	public void push(CallInfo ci) {
		Check.notNull(ci);
		callStack.addFirst(ci);
	}

	public void push(Resumable resumable, Serializable suspendedState) {
		throw new UnsupportedOperationException();  // TODO
	}

	public void pushCall(Invokable function, int base, int ret, int pc, int numResults, int flags) {
		push(new CallInfo(function, base, ret, pc, numResults, flags));
	}

	// LIFO iterator
	public ListIterator<CallInfo> frameIterator() {
		return Collections.unmodifiableList(callStack).listIterator();
	}

}

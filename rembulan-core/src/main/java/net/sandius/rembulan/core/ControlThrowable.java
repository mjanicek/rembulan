package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

public abstract class ControlThrowable extends Throwable {

	protected final LinkedList<CallInfo> callStack;
	protected final ArrayList<ResumeInfo> resumeStack;

	protected ControlThrowable() {
		this.callStack = new LinkedList<>();
		this.resumeStack = new ArrayList<>();
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	@Deprecated
	public void push(CallInfo ci) {
		Check.notNull(ci);
		callStack.addFirst(ci);
	}

	public void push(Resumable resumable, Serializable suspendedState) {
		resumeStack.add(0, new ResumeInfo(resumable, suspendedState));
	}

	// LIFO iterator
	public ListIterator<CallInfo> frameIterator() {
		return Collections.unmodifiableList(callStack).listIterator();
	}

	public ArrayList<ResumeInfo> resumeStack() {
		return resumeStack;
	}

}

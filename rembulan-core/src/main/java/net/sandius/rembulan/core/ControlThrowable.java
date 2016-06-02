package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Cons;

import java.util.Iterator;

public abstract class ControlThrowable extends Throwable {

	private Cons<ResumeInfo> resumeStack;

	protected ControlThrowable(Cons<ResumeInfo> resumeStack) {
		this.resumeStack = resumeStack;
	}

	protected ControlThrowable() {
		this(null);
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	public void push(Resumable resumable, Object suspendedState) {
		resumeStack = new Cons<>(new ResumeInfo(resumable, suspendedState), resumeStack);
	}

	// LIFO iterator
	public Iterator<ResumeInfo> frames() {
		return Cons.newIterator(resumeStack);
	}

	protected Cons<ResumeInfo> resumeStack() {
		return resumeStack;
	}

	public abstract Preemption toPreemption();

}

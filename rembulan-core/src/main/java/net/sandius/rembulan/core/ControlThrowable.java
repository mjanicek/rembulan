package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Cons;

import java.io.Serializable;
import java.util.Iterator;

public abstract class ControlThrowable extends Throwable {

	private Cons<ResumeInfo> resumeStack;

	protected ControlThrowable() {
		this.resumeStack = null;
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	public void push(Resumable resumable, Serializable suspendedState) {
		resumeStack = new Cons<>(new ResumeInfo(resumable, suspendedState), resumeStack);
	}

	// LIFO iterator
	public Iterator<ResumeInfo> frames() {
		return Cons.newIterator(resumeStack);
	}

}

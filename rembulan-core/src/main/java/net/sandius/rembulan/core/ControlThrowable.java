package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Cons;

import java.util.Iterator;

public abstract class ControlThrowable extends Throwable {

	private Cons<ResumeInfo> resumeStack;

	protected ControlThrowable() {
		super(null, null, true, false);
		this.resumeStack = null;
	}

	public void push(Resumable resumable, Object suspendedState) {
		resumeStack = new Cons<>(new ResumeInfo(resumable, suspendedState), resumeStack);
	}

	// LIFO iterator
	public Iterator<ResumeInfo> frames() {
		return Cons.newIterator(resumeStack);
	}

}

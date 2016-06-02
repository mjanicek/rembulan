package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Cons;

public class Preempted extends ControlThrowable {

	Preempted(Cons<ResumeInfo> resumeStack) {
		super(resumeStack);
	}

	public Preempted() {
		this(null);
	}

	@Override
	public Preemption toPreemption() {
		return new Preemption.Pause(resumeStack());
	}

}

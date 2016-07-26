package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public class WaitForAsync extends ControlThrowable {

	private final Runnable task;

	public WaitForAsync(Runnable task) {
		this.task = Check.notNull(task);
	}

	public Runnable task() {
		return task;
	}

}

package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public class WaitForAsync extends ControlThrowable {

	private final AsyncTask<?> task;

	public WaitForAsync(AsyncTask<?> task) {
		this.task = Check.notNull(task);
	}

	public AsyncTask<?> task() {
		return task;
	}

}

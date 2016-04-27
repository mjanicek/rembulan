package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public class ResumeInfo {

	public final Resumable resumable;
	public final Object savedState;

	public ResumeInfo(Resumable resumable, Object savedState) {
		this.resumable = Check.notNull(resumable);
		this.savedState = savedState;
	}

	public void resume(ExecutionContext context) throws ControlThrowable {
		resumable.resume(context, savedState);
	}

}

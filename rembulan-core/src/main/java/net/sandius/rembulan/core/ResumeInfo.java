package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.io.Serializable;

public class ResumeInfo {

	public final Resumable resumable;
	public final Serializable savedState;

	public ResumeInfo(Resumable resumable, Serializable savedState) {
		this.resumable = Check.notNull(resumable);
		this.savedState = savedState;
	}

	public void resume(LuaState state, ObjectSink result) throws ControlThrowable {
		resumable.resume(state, result, savedState);
	}

}

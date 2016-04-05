package net.sandius.rembulan.core;

public class ResumeInfo {

	public final Resumable function;
	public final Object savedState;

	public ResumeInfo(Resumable function, Object savedState) {
		this.function = function;
		this.savedState = savedState;
	}

}

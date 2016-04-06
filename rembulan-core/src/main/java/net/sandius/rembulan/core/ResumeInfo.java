package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public class ResumeInfo {

	public final Resumable function;
	public final Object savedState;

	public ResumeInfo(Resumable function, Object savedState) {
		this.function = function;
		this.savedState = savedState;
	}

	public static class SavedState {

		public final int resumptionPoint;
		public final Object[] registers;
		public final Object[] varargs;

		public SavedState(int resumptionPoint, Object[] registers, Object[] varargs) {
			this.resumptionPoint = resumptionPoint;
			this.registers = Check.notNull(registers);
			this.varargs = varargs;
		}

		public SavedState(int resumptionPoint, Object[] registers) {
			this(resumptionPoint, registers, null);
		}

	}

}

package net.sandius.rembulan.core;

import net.sandius.rembulan.util.ReadOnlyArray;

public class ResumeInfo {

	public final Resumable function;
	public final Object savedState;

	public ResumeInfo(Resumable function, Object savedState) {
		this.function = function;
		this.savedState = savedState;
	}

	public static class SavedState {

		public final int resumptionPoint;
		public final ReadOnlyArray<Object> registers;
		public final ReadOnlyArray<Object> varargs;

		public SavedState(int resumptionPoint, Object[] registers, Object[] varargs) {
			this.resumptionPoint = resumptionPoint;
			this.registers = ReadOnlyArray.copyFrom(registers);
			this.varargs = ReadOnlyArray.copyFrom(varargs);
		}

		public SavedState(int resumptionPoint, Object[] registers) {
			this(resumptionPoint, registers, new Object[0]);
		}

	}

}

package net.sandius.rembulan.core;

public abstract class PreemptionContext {

	public abstract boolean shouldPreemptNow();

	public static class Const extends PreemptionContext {

		public final boolean value;

		public Const(boolean value) {
			this.value = value;
		}

		@Override
		public boolean shouldPreemptNow() {
			return value;
		}

	}

	public static final Const ALWAYS = new Const(true);
	public static final Const NEVER = new Const(false);

}

package net.sandius.rembulan.core;

public interface PreemptionContext {

	void withdraw(int cost) throws ControlThrowable;

	class Always implements PreemptionContext {

		public static final Always INSTANCE = new Always();

		@Override
		public void withdraw(int cost) throws ControlThrowable {
			throw new Preempted();

		}
	}

	class Never implements PreemptionContext {

		public static final Never INSTANCE = new Never();

		@Override
		public void withdraw(int cost) throws ControlThrowable {
			// no-op
		}

	}

}

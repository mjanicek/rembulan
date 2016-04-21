package net.sandius.rembulan.core;

public interface PreemptionContext {

	void withdraw(int cost) throws ControlThrowable;

	abstract class AbstractPreemptionContext implements PreemptionContext {
		protected final void preempt() throws ControlThrowable {
			throw new Preempted();
		}
	}

	class Always extends AbstractPreemptionContext {

		public static final Always INSTANCE = new Always();

		@Override
		public void withdraw(int cost) throws ControlThrowable {
			preempt();
		}
	}

	class Never extends AbstractPreemptionContext {

		public static final Never INSTANCE = new Never();

		@Override
		public void withdraw(int cost) throws ControlThrowable {
			// no-op
		}

	}

}

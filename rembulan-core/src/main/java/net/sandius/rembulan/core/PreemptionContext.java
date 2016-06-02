package net.sandius.rembulan.core;

public interface PreemptionContext {

	Preemption withdraw(int cost);

	abstract class AbstractPreemptionContext implements PreemptionContext {

		protected final Preemption preempt() {
			return new Preemption.Pause();
		}

	}

	class Always extends AbstractPreemptionContext {

		public static final Always INSTANCE = new Always();

		@Override
		public Preemption withdraw(int cost) {
			return preempt();
		}
	}

	class Never extends AbstractPreemptionContext {

		public static final Never INSTANCE = new Never();

		@Override
		public Preemption withdraw(int cost) {
			// no-op
			return null;
		}

	}

}

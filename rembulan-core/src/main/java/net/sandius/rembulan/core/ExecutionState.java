package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public abstract class ExecutionState {

	public static final ExecutionState RUNNING = new ExecutionState.Running();
	public static final ExecutionState TERMINATED_NORMALLY = new ExecutionState.TerminatedNormally();
	public static final ExecutionState PAUSED = new ExecutionState.Paused();

	public abstract boolean isTerminated();

	public abstract boolean isRunning();

	public static final class Running extends ExecutionState {

		private Running() {
			// not to be instantiated by the outside world
		}

		@Override
		public boolean isTerminated() {
			return false;
		}

		@Override
		public boolean isRunning() {
			return true;
		}

	}

	static abstract class Terminated extends ExecutionState {

		private Terminated() {
			// not to be instantiated by the outside world
		}

		@Override
		public final boolean isTerminated() {
			return true;
		}

		@Override
		public final boolean isRunning() {
			return false;
		}

	}

	public static final class TerminatedNormally extends Terminated {

		private TerminatedNormally() {
			// not to be instantiated by the outside world
		}

	}

	public static final class TerminatedAbnormally extends Terminated {

		private final Throwable error;

		public TerminatedAbnormally(Throwable error) {
			this.error = Check.notNull(error);
		}

		public Throwable getError() {
			return error;
		}

	}

	public static class Paused extends ExecutionState {

		private Paused() {
			// not to be instantiated by the outside world
		}

		@Override
		public boolean isTerminated() {
			return false;
		}

		@Override
		public boolean isRunning() {
			return false;
		}

	}

}

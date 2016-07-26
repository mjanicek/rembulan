package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public abstract class ResumeResult {

	private ResumeResult() {
		// not to be instantiated by the outside
	}

	public static class Finished extends ResumeResult {

		public static final Finished INSTANCE = new Finished();

		private Finished() {
			// not to be instantiated by the outside
		}

	}

	public static class Error extends ResumeResult {

		public final Throwable error;

		public Error(Throwable error) {
			this.error = Check.notNull(error);
		}

	}

	public static class Pause extends ResumeResult {

		public static final Pause INSTANCE = new Pause();

		private Pause() {
			// not to be instantiated by the outside
		}

	}

	public static class WaitForAsync extends ResumeResult {

		public final AsyncTask task;

		public WaitForAsync(AsyncTask task) {
			this.task = task;
		}

	}

	public static class Switch extends ResumeResult {

		public final Coroutine target;

		public Switch(Coroutine target) {
			this.target = Check.notNull(target);
		}

	}

	public static class ImplicitYield extends ResumeResult {

		public final Coroutine target;
		public final Throwable error;

		public ImplicitYield(Coroutine target, Throwable error) {
			this.target = Check.notNull(target);
			this.error = error;
		}

	}

}

package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public class Exec {

	private final LuaState state;
	private final ObjectSink objectSink;

	private final Context context;

	private Coroutine currentCoroutine;

	public Exec(LuaState state) {
		this.state = Check.notNull(state);
		this.objectSink = state.newObjectSink();
		this.context = new Context();
	}

	public LuaState getState() {
		return state;
	}

	public ObjectSink getSink() {
		return objectSink;
	}

	public boolean isPaused() {
		return currentCoroutine != null && currentCoroutine.isPaused();
	}

	protected Coroutine getCurrentCoroutine() {
		return currentCoroutine;
	}

	public ExecutionContext getContext() {
		return context;
	}

	protected class Context implements ExecutionContext {

		@Override
		public LuaState getState() {
			return state;
		}

		@Override
		public ObjectSink getObjectSink() {
			return objectSink;
		}

		@Override
		public Coroutine getCurrentCoroutine() {
			return currentCoroutine;
		}

		@Override
		public Coroutine newCoroutine(Function function) {
			return new Coroutine(function);
		}

		@Override
		public boolean canYield() {
			return currentCoroutine.canYield();
		}

	}

	public void init(Function target, Object... args) {
		Check.notNull(target);
		Check.notNull(args);

		if (currentCoroutine != null) {
			throw new IllegalStateException("Initialising call in paused state");
		}
		else {
			Coroutine c = context.newCoroutine(target);
			objectSink.setToArray(args);
			currentCoroutine = c;
		}
	}

	// return null if main coroutine returned, otherwise return next coroutine C to be resumed;
	// if C == coro, then this is a pause

	// return true if execution was paused, false if execution is finished
	// in other words: returns true iff isPaused() == true afterwards
	public boolean resume() {
		Throwable error = null;

		while (currentCoroutine != null) {
			Coroutine.ResumeResult result = currentCoroutine.resume(context, error);

			if (result == Coroutine.ResumeResult.PAUSED) {
				// pause
				return true;
			}
			else {
				// coroutine switch
				currentCoroutine = result.coroutine;
				error = result.error;
			}
		}

		if (error == null) {
			// main coroutine returned
			return false;
		}
		else {
			// exception in the main coroutine: rethrow
			throw new ExecutionException(error);
		}
	}

}

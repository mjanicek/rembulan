package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

import java.util.concurrent.atomic.AtomicReference;

public class Exec {

	private final AtomicReference<ExecutionState> executionState;

	private final LuaState state;
	private final ObjectSink objectSink;

	private final Context context;

	private Coroutine currentCoroutine;

	public Exec(LuaState state, Function target, Object... args) {
		this.state = Check.notNull(state);
		this.objectSink = state.newObjectSink();
		this.context = new Context();

		this.executionState = new AtomicReference<>(ExecutionState.PAUSED);

		Check.notNull(target);
		Check.notNull(args);

		Coroutine c = context.newCoroutine(target);
		objectSink.setToArray(args);
		currentCoroutine = c;
	}

	public ExecutionState getExecutionState() {
		return executionState.get();
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

	// return true if execution was paused, false if execution is finished
	// in other words: returns true iff isPaused() == true afterwards
	public ExecutionState resume() {
		Throwable error = null;

		while (currentCoroutine != null) {
			ResumeResult result = currentCoroutine.resume(context, error);

			if (result instanceof ResumeResult.Pause) {
				ExecutionState es = ExecutionState.PAUSED;
				executionState.set(es);
				return es;
			}
			else if (result instanceof ResumeResult.Switch) {
				currentCoroutine = ((ResumeResult.Switch) result).target;
				error = null;
			}
			else if (result instanceof ResumeResult.ImplicitYield) {
				ResumeResult.ImplicitYield r = (ResumeResult.ImplicitYield) result;
				currentCoroutine = r.target;
				error = r.error;
			}
			else if (result instanceof ResumeResult.Error) {
				currentCoroutine = null;
				error = ((ResumeResult.Error) result).error;
			}
			else if (result instanceof ResumeResult.Finished) {
				currentCoroutine = null;
				error = null;
			}
			else {
				throw new IllegalStateException("Illegal result: " + result);
			}
		}

		if (error == null) {
			// main coroutine returned
			ExecutionState es = ExecutionState.TERMINATED_NORMALLY;
			executionState.set(es);
			return es;
		}
		else {
			// exception in the main coroutine
			ExecutionState es = new ExecutionState.TerminatedAbnormally(error);
			executionState.set(es);
			return es;
		}
	}

}

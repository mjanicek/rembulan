package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

import java.io.Serializable;
import java.util.Iterator;

public class Exec {

	private final LuaState state;
	private final Coroutine mainCoroutine;

	private final Context context;

	public Exec(LuaState state) {
		this.state = Check.notNull(state);
		mainCoroutine = Check.notNull(state.newCoroutine());
		this.context = new Context();
	}

	public LuaState getState() {
		return state;
	}

	public ObjectSink getSink() {
		return mainCoroutine.objectSink();
	}

	public boolean isPaused() {
		return mainCoroutine.callStack != null;
	}

	public Coroutine getMainCoroutine() {
		return mainCoroutine;
	}

	protected Coroutine getCurrentCoroutine() {
		// FIXME
		return getMainCoroutine();
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
			return Exec.this.getCurrentCoroutine().objectSink();
		}

		@Override
		public Coroutine getCurrentCoroutine() {
			return Exec.this.getCurrentCoroutine();
		}

	}

	protected static class BootstrapResumable implements Resumable {

		static final BootstrapResumable INSTANCE = new BootstrapResumable();

		@Override
		public void resume(LuaState state, ObjectSink result, Serializable suspendedState) throws ControlThrowable {
			Call c = (Call) suspendedState;
			Dispatch.call(state, result, c.target, c.args);
		}

		private static class Call implements Serializable {
			public final Object target;
			public final Object[] args;
			public Call(Object target, Object[] args) {
				this.target = Check.notNull(target);
				this.args = Check.notNull(args);
			}
		}

		public static ResumeInfo of(Object target, Object... args) {
			return new ResumeInfo(INSTANCE, new Call(target, args));
		}

	}

	public void init(Object target, Object... args) {
		Check.notNull(args);

		if (mainCoroutine.callStack != null) {
			throw new IllegalStateException("Initialising call in paused state");
		}
		else {
			mainCoroutine.callStack = new Cons<>(BootstrapResumable.of(target, args));
		}
	}

	// return true if execution was paused, false if execution is finished
	// in other words: returns true iff isPaused() == true afterwards
	public boolean resume() {
		Coroutine coro = mainCoroutine;

		while (coro.callStack != null) {
			ResumeInfo top = coro.callStack.car;
			coro.callStack = coro.callStack.cdr;

			try {
				top.resume(state, coro.sink);
				Dispatch.evaluateTailCalls(state, coro.sink);
			}
			catch (ControlThrowable ct) {
				Iterator<ResumeInfo> it = ct.frames();
				while (it.hasNext()) {
					coro.callStack = new Cons<>(it.next(), coro.callStack);
				}

				assert (coro.callStack != null);
				return true;  // we're paused
			}
		}

		// call stack is null, we're not paused
		return false;
	}

}

package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

import java.io.Serializable;
import java.util.Iterator;

public class Exec {

	private final LuaState state;
	private final ObjectSink sink;

	private Cons<ResumeInfo> callStack;

	public Exec(LuaState state) {
		this.state = Check.notNull(state);
		this.sink = Check.notNull(state.newObjectSink());
		callStack = null;
	}

	public LuaState getState() {
		return state;
	}

	public ObjectSink getSink() {
		return sink;
	}

	public boolean isPaused() {
		return callStack != null;
	}

	public Cons<ResumeInfo> getCallStack() {
		return callStack;
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

		if (callStack != null) {
			throw new IllegalStateException("Initialising call in paused state");
		}
		else {
			callStack = new Cons<>(BootstrapResumable.of(target, args));
		}
	}

	// return true if execution was paused, false if execution is finished
	// in other words: returns true iff isPaused() == true afterwards
	public boolean resume() {
		while (callStack != null) {
			ResumeInfo top = callStack.car;
			callStack = callStack.cdr;

			try {
				top.resume(state, sink);
				Dispatch.evaluateTailCalls(state, sink);
			}
			catch (ControlThrowable ct) {
				Iterator<ResumeInfo> it = ct.frames();
				while (it.hasNext()) {
					callStack = new Cons<>(it.next(), callStack);
				}

				assert (callStack != null);
				return true;  // we're paused
			}
		}

		// call stack is null, we're not paused
		return false;
	}

}

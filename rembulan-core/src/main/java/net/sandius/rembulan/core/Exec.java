package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

import java.util.Iterator;

public class Exec {

	private final PreemptionContext preemptionContext;
	private final LuaState state;

	private final ObjectStack objectStack;

	private Cons<CallInfo> callStack;

	public Exec(PreemptionContext preemptionContext, LuaState state, ObjectStack objectStack) {
		Check.notNull(preemptionContext);
		Check.notNull(state);
		Check.notNull(objectStack);

		this.preemptionContext = preemptionContext;
		this.state = state;
		this.objectStack = objectStack;

		callStack = null;
	}

	public PreemptionContext getPreemptionContext() {
		return preemptionContext;
	}

	public LuaState getState() {
		return state;
	}

	public ObjectStack getObjectStack() {
		return objectStack;
	}

	public boolean isPaused() {
		return callStack != null;
	}

	public Cons<CallInfo> getCallStack() {
		return callStack;
	}

	public void pushCall(CallInfo ci) {
		Check.notNull(ci);

		if (callStack != null) {
			throw new IllegalStateException("Pushing a call in paused state");
		}
		else {
			callStack = new Cons<>(ci);
		}
	}

	// return true if execution was paused, false if execution is finished
	// in other words: returns true iff isPaused() == true afterwards
	public boolean resume() {
		while (callStack != null) {
			CallInfo top = callStack.car;
			callStack = callStack.cdr;

			try {
				top.resume(preemptionContext, state, objectStack);
			}
			catch (ControlThrowable ct) {
				Iterator<CallInfo> it = ct.frameIterator();
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

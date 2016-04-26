package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

import java.io.Serializable;
import java.util.Iterator;

public class Exec {

	private final LuaState state;
	private final ObjectSink objectSink;

	private final Context context;

	private Coroutine mainCoroutine;
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
		return mainCoroutine.callStack != null;
	}

	public Coroutine getMainCoroutine() {
		return mainCoroutine;
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
			CoroutineImpl coroutine = new CoroutineImpl(state);
			coroutine.callStack = new Cons<>(new ResumeInfo(CoroutineBootstrapResumable.INSTANCE, new SerializableFunction(function)));
			return coroutine;
		}

		@Override
		public boolean canYield() {
			return currentCoroutine != mainCoroutine;
		}

	}

	protected class CoroutineImpl extends Coroutine {

		public CoroutineImpl(LuaState state) {
			super(state);
		}

		@Override
		public Status getStatus() {
			if (this == currentCoroutine) return Status.Running;
			else if (callStack == null) return Status.Dead;
			else if (resuming != null) return Status.Normal;
			else return Status.Suspended;
		}

	}

	@Deprecated
	protected static class SerializableFunction implements Serializable {
		public final Function f;
		public SerializableFunction(Function f) {
			this.f = Check.notNull(f);
		}
	}

	protected static class CoroutineBootstrapResumable implements Resumable {

		static final CoroutineBootstrapResumable INSTANCE = new CoroutineBootstrapResumable();

		@Override
		public void resume(ExecutionContext context, Serializable suspendedState) throws ControlThrowable {
			Function target = ((SerializableFunction) suspendedState).f;
			Dispatch.call(context, target, context.getObjectSink().toArray());
		}
	}

	protected static class BootstrapResumable implements Resumable {

		static final BootstrapResumable INSTANCE = new BootstrapResumable();

		@Override
		public void resume(ExecutionContext context, Serializable suspendedState) throws ControlThrowable {
			Call c = (Call) suspendedState;
			Dispatch.call(context, c.target, c.args);
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

	public void init(Function target, Object... args) {
		Check.notNull(target);
		Check.notNull(args);

		if (mainCoroutine != null) {
			throw new IllegalStateException("Initialising call in paused state");
		}
		else {
			mainCoroutine = context.newCoroutine(target);
			currentCoroutine = mainCoroutine;

			mainCoroutine.callStack = new Cons<>(BootstrapResumable.of(target, args));
		}
	}

	private Cons<ResumeInfo> prependCalls(Iterator<ResumeInfo> it, Cons<ResumeInfo> tail) {
		while (it.hasNext()) {
			tail = new Cons<>(it.next(), tail);
		}
		return tail;
	}

	// return null if main coroutine returned, otherwise return next coroutine C to be resumed;
	// if C == coro, then this is a pause
	private Coroutine resumeCoroutine(final Coroutine coro) {
		Check.isNull(coro.resuming);

		Cons<ResumeInfo> callStack = coro.callStack;

		while (callStack != null) {
			ResumeInfo top = callStack.car;
			callStack = callStack.cdr;

			try {
				top.resume(context);
				Dispatch.evaluateTailCalls(context);
			}
			catch (CoroutineSwitch.Yield yield) {
				callStack = prependCalls(yield.frames(), callStack);

				Coroutine target = coro.yieldingTo;

				if (target != null) {
					objectSink.setToArray(yield.args);

					coro.yieldingTo = null;
					target.resuming = null;

					return target;
				}
				else {
					// XXX cannot yield outside a coroutine
					throw new IllegalOperationAttemptException("attempt to yield from outside a coroutine");
				}
			}
			catch (CoroutineSwitch.Resume resume) {
				callStack = prependCalls(resume.frames(), callStack);

				Coroutine target = resume.coroutine;

				if (target.resuming == null && target.callStack != null) {
					objectSink.setToArray(resume.args);

					target.yieldingTo = coro;
					coro.resuming = target;

					return target;
				}
				else {
					// XXX cannot resume
					throw new IllegalStateException("cannot resume a non-suspended coroutine " + target + ", status=" + target.getStatus());
				}
			}
			catch (Preempted preempted) {
				callStack = prependCalls(preempted.frames(), callStack);
				assert (callStack != null);
				return coro;
			}
			catch (ControlThrowable ct) {
				throw new UnsupportedOperationException(ct);
			}
			finally {
				coro.callStack = callStack;
			}
		}

		Coroutine yieldTarget = coro.yieldingTo;
		if (yieldTarget != null) {
			// implicit yield on return
			coro.yieldingTo = null;
			yieldTarget.resuming = null;
			return yieldTarget;
		}
		else {
			// main coroutine return
			return null;
		}
	}

	// return true if execution was paused, false if execution is finished
	// in other words: returns true iff isPaused() == true afterwards
	public boolean resume() {
		while (currentCoroutine != null) {
			Coroutine next = resumeCoroutine(currentCoroutine);
			if (next == currentCoroutine) {
				// pause
				return true;
			}
			else {
				// coroutine switch
				currentCoroutine = next;
			}
		}

		// main coroutine returned
		return false;
	}

}

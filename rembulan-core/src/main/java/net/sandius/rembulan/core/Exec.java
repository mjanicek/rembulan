package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

import java.io.Serializable;
import java.util.Iterator;

public class Exec {

	private final LuaState state;

	private final Context context;

	private Coroutine mainCoroutine;
	private Coroutine currentCoroutine;

	public Exec(LuaState state) {
		this.state = Check.notNull(state);
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
			return Exec.this.getCurrentCoroutine().objectSink();
		}

		@Override
		public Coroutine getCurrentCoroutine() {
			return Exec.this.getCurrentCoroutine();
		}

		@Override
		public Coroutine newCoroutine(Function function) {
			CoroutineImpl coroutine = new CoroutineImpl(state);
			coroutine.callStack = new Cons<>(new ResumeInfo(CoroutineBootstrapResumable.INSTANCE, new SerializableFunction(function)));
			return coroutine;
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

		Function func = (Function) target;

		if (mainCoroutine != null) {
			throw new IllegalStateException("Initialising call in paused state");
		}
		else {
			mainCoroutine = context.newCoroutine(func);
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

	// return true if execution was paused, false if execution is finished
	// in other words: returns true iff isPaused() == true afterwards
	public boolean resume() {
		while (currentCoroutine.callStack != null) {
			ResumeInfo top = currentCoroutine.callStack.car;
			currentCoroutine.callStack = currentCoroutine.callStack.cdr;

			try {
				top.resume(context);
				Dispatch.evaluateTailCalls(context);
			}
			catch (CoroutineSwitch.Yield yield) {
				currentCoroutine.callStack = prependCalls(yield.frames(), currentCoroutine.callStack);

				Object[] args = yield.args;

				if (currentCoroutine.yieldingTo != null) {

					Coroutine target = currentCoroutine.yieldingTo;
					target.objectSink().setToArray(args);

					currentCoroutine.yieldingTo = null;
					target.resuming = null;

					currentCoroutine = target;

					continue;
				}
				else {
					// XXX cannot yield outside a coroutine
					throw new IllegalOperationAttemptException("attempt to yield from outside a coroutine");
				}
			}
			catch (CoroutineSwitch.Resume resume) {
				currentCoroutine.callStack = prependCalls(resume.frames(), currentCoroutine.callStack);

				Coroutine target = resume.coroutine;

				if (target.resuming == null && target.callStack != null) {
					Object[] args = resume.args;
					target.objectSink().setToArray(args);

					target.yieldingTo = currentCoroutine;
					currentCoroutine.resuming = target;

					currentCoroutine = target;

					continue;
				}
				else {
					// XXX cannot resume
					throw new IllegalStateException("cannot resume a non-suspended coroutine " + target + ", status=" + target.getStatus());
				}
			}
			catch (Preempted preempted) {
				currentCoroutine.callStack = prependCalls(preempted.frames(), currentCoroutine.callStack);
				assert (currentCoroutine.callStack != null);
				return true;  // we're paused
			}
			catch (ControlThrowable ct) {
				throw new UnsupportedOperationException(ct);
			}

			if (currentCoroutine.callStack == null) {
				Coroutine yieldTarget = currentCoroutine.yieldingTo;
				// coroutine terminated normally, this is an implicit yield
				if (yieldTarget != null) {
					Object[] args = currentCoroutine.objectSink().toArray();
					yieldTarget.objectSink().setToArray(args);
					currentCoroutine.yieldingTo = null;
					yieldTarget.resuming = null;
					currentCoroutine = yieldTarget;
				}
			}

		}

		// call stack is null, we're not paused
		return false;
	}

}

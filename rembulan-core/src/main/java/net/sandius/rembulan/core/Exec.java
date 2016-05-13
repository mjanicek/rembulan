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
			return new Coroutine(Exec.this, function);
		}

		@Override
		public boolean canYield() {
			return currentCoroutine != mainCoroutine;
		}

	}

	protected static class BootstrapResumable implements Resumable {

		static final BootstrapResumable INSTANCE = new BootstrapResumable();

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			Call c = (Call) suspendedState;
			Dispatch.call(context, c.target, c.args);
		}

		private static class Call {
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

	private static class CoroutineResumeResult {
		public static final CoroutineResumeResult PAUSED = new CoroutineResumeResult(null, null);

		public final Coroutine coroutine;
		public final Throwable error;

		private CoroutineResumeResult(Coroutine coroutine, Throwable error) {
			this.coroutine = coroutine;
			this.error = error;
		}

		public static CoroutineResumeResult switchTo(Coroutine c) {
			return switchTo(c, null);
		}

		public static CoroutineResumeResult switchTo(Coroutine c, Throwable e) {
			Check.notNull(c);
			return new CoroutineResumeResult(c, e);
		}

		public static CoroutineResumeResult errorInCoroutine(Coroutine c, Throwable e) {
			Check.notNull(c);
			Check.notNull(e);
			return new CoroutineResumeResult(c, e);
		}

		public static CoroutineResumeResult mainReturn(Throwable e) {
			return new CoroutineResumeResult(null, e);
		}

	}

	private CoroutineResumeResult doYield(Coroutine current, Object[] args) {
		Coroutine target = current.yieldingTo;

		if (target != null) {
			objectSink.setToArray(args);

			assert (current.resuming == null);

			current.yieldingTo = null;
			target.resuming = null;

			return CoroutineResumeResult.switchTo(target);
		}
		else {
			return CoroutineResumeResult.errorInCoroutine(current,
					new IllegalOperationAttemptException("attempt to yield from outside a coroutine"));
		}
	}

	private CoroutineResumeResult doResume(Coroutine current, Coroutine target, Object[] args) {
		if (target.callStack == null) {
			// dead coroutine
			return CoroutineResumeResult.errorInCoroutine(current,
					new IllegalStateException("cannot resume dead coroutine"));
		}
		else if (target == current || target.resuming != null) {
			// running or normal coroutine
			return CoroutineResumeResult.errorInCoroutine(current,
					new IllegalStateException("cannot resume non-suspended coroutine"));
		}
		else {
			objectSink.setToArray(args);

			target.yieldingTo = current;
			current.resuming = target;

			return CoroutineResumeResult.switchTo(target);
		}
	}

	// return null if main coroutine returned, otherwise return next coroutine C to be resumed;
	// if C == coro, then this is a pause
	private CoroutineResumeResult resumeCoroutine(final Coroutine coro, Throwable error) {
		Check.isNull(coro.resuming);

		Cons<ResumeInfo> callStack = coro.callStack;

		try {

			while (callStack != null) {
				ResumeInfo top = callStack.car;
				callStack = callStack.cdr;

				try {
					if (error == null) {
						// no errors
						top.resume(context);
						Dispatch.evaluateTailCalls(context);
					}
					else {
						// there is an error to be handled
						if (top.resumable instanceof ProtectedResumable) {
							// top is protected, can handle the error
							Throwable e = error;
							error = null;  // this exception will be handled

							ProtectedResumable pr = (ProtectedResumable) top.resumable;
							pr.resumeError(context, top.savedState, Conversions.throwableToObject(e));
							Dispatch.evaluateTailCalls(context);
						}
						else {
							// top is not protected, continue unwinding the stack
						}
					}
				}
				catch (CoroutineSwitch.Yield yield) {
					callStack = prependCalls(yield.frames(), callStack);
					return doYield(coro, yield.args);
				}
				catch (CoroutineSwitch.Resume resume) {
					callStack = prependCalls(resume.frames(), callStack);
					return doResume(coro, resume.coroutine, resume.args);
				}
				catch (Preempted preempted) {
					callStack = prependCalls(preempted.frames(), callStack);
					assert (callStack != null);
					return CoroutineResumeResult.PAUSED;
				}
				catch (ControlThrowable ct) {
					throw new UnsupportedOperationException(ct);
				}
				catch (Exception ex) {
					// unhandled exception: will try finding a handler in the next iteration
					error = ex;
				}
			}
		}
		finally {
			coro.callStack = callStack;
		}

		assert (coro.callStack == null);

		Coroutine yieldTarget = coro.yieldingTo;
		if (yieldTarget != null) {
			// implicit yield on return
			coro.yieldingTo = null;
			yieldTarget.resuming = null;
			return new CoroutineResumeResult(yieldTarget, error);
		}
		else {
			// main coroutine return
			return CoroutineResumeResult.mainReturn(error);
		}
	}

	// return true if execution was paused, false if execution is finished
	// in other words: returns true iff isPaused() == true afterwards
	public boolean resume() {
		Throwable error = null;

		while (currentCoroutine != null) {
			CoroutineResumeResult result = resumeCoroutine(currentCoroutine, error);

			if (result == CoroutineResumeResult.PAUSED) {
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

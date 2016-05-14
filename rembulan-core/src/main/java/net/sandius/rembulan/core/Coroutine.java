package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

import java.util.Iterator;

/*
 Properties:

   1) For any coroutine c,
         (c.resuming == null || c.resuming.yieldingTo == c) && (c.yieldingTo == null || c.yieldingTo.resuming == c)
         (i.e. coroutines form a doubly-linked list)

   2) if c is the currently-running coroutine, then c.resuming == null

   3) if c is the main coroutine, then c.yieldingTo == null

 Coroutine d can be resumed from c iff
   c != d && d.resuming == null && d.yieldingTo == null

 This means that
   c.resuming = d
   d.yieldingTo = c
 */
public final class Coroutine {

	// paused call stack: up-to-date only iff coroutine is not running
	private Cons<ResumeInfo> callStack;

	private Coroutine yieldingTo;
	private Coroutine resuming;

	public Coroutine(Function function) {
		this.callStack = new Cons<>(new ResumeInfo(BootstrapResumable.INSTANCE, Check.notNull(function)));
		this.yieldingTo = null;
		this.resuming = null;
	}

	public boolean isPaused() {
		return callStack != null;
	}

	public boolean isResuming() {
		return resuming != null;
	}

	public boolean isDead() {
		return callStack == null;
	}

	public boolean canYield() {
		return yieldingTo != null;
	}

	@Override
	public String toString() {
		return "thread: 0x" + Integer.toHexString(hashCode());
	}

	protected static class BootstrapResumable implements Resumable {

		static final BootstrapResumable INSTANCE = new BootstrapResumable();

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			Function target = (Function) suspendedState;
			Dispatch.call(context, target, context.getObjectSink().toArray());
		}

	}

	// FIXME: name clash
	private Coroutine resume(Coroutine target) {
		Check.notNull(target);

		synchronized (this) {
			synchronized (target) {

				if (target.callStack == null) {
					// dead coroutine
					throw new IllegalStateException("cannot resume dead coroutine");
				}
				else if (target == this || target.resuming != null) {
					// running or normal coroutine
					throw new IllegalStateException("cannot resume non-suspended coroutine");
				}
				else {
					target.yieldingTo = this;
					this.resuming = target;

					return target;
				}
			}
		}
	}

	private Coroutine yield() {
		synchronized (this) {
			Coroutine target = this.yieldingTo;

			if (target != null) {
				synchronized (target) {  // FIXME: unsafe: target may have been changed already!

					assert (this.resuming == null);
					assert (target.resuming == this);

					this.yieldingTo = null;
					target.resuming = null;

					return target;
				}
			}
			else {
				return null;
			}
		}
	}

	private Cons<ResumeInfo> prependCalls(Iterator<ResumeInfo> it, Cons<ResumeInfo> tail) {
		while (it.hasNext()) {
			tail = new Cons<>(it.next(), tail);
		}
		return tail;
	}

	static class ResumeResult {
		public static final ResumeResult PAUSED = new ResumeResult(null, null);

		public final Coroutine coroutine;
		public final Throwable error;

		private ResumeResult(Coroutine coroutine, Throwable error) {
			this.coroutine = coroutine;
			this.error = error;
		}

		public static ResumeResult switchTo(Coroutine c) {
			return switchTo(c, null);
		}

		public static ResumeResult switchTo(Coroutine c, Throwable e) {
			Check.notNull(c);
			return new ResumeResult(c, e);
		}

		public static ResumeResult errorInCoroutine(Coroutine c, Throwable e) {
			Check.notNull(c);
			Check.notNull(e);
			return new ResumeResult(c, e);
		}

		public static ResumeResult mainReturn(Throwable e) {
			return new ResumeResult(null, e);
		}

	}

	private ResumeResult doYield(ObjectSink objectSink, Object[] args) {
		Coroutine c = this.yield();
		if (c != null) {
			objectSink.setToArray(args);
			return ResumeResult.switchTo(c);
		}
		else {
			return ResumeResult.errorInCoroutine(this,
					new IllegalOperationAttemptException("attempt to yield from outside a coroutine"));
		}
	}

	private ResumeResult doResume(ObjectSink objectSink, Coroutine target, Object[] args) {
		final Coroutine c;
		try {
			c = this.resume(target);
		}
		catch (Exception ex) {
			return ResumeResult.errorInCoroutine(this, ex);
		}

		objectSink.setToArray(args);
		return ResumeResult.switchTo(c);
	}

	public ResumeResult resume(ExecutionContext context, Throwable error) {
		Check.isNull(resuming);

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
				return doYield(context.getObjectSink(), yield.args);
			}
			catch (CoroutineSwitch.Resume resume) {
				callStack = prependCalls(resume.frames(), callStack);
				return doResume(context.getObjectSink(), resume.coroutine, resume.args);
			}
			catch (Preempted preempted) {
				callStack = prependCalls(preempted.frames(), callStack);
				assert (callStack != null);
				return ResumeResult.PAUSED;
			}
			catch (ControlThrowable ct) {
				throw new UnsupportedOperationException(ct);
			}
			catch (Exception ex) {
				// unhandled exception: will try finding a handler in the next iteration
				error = ex;
			}
		}

		assert (callStack == null);

		Coroutine yieldTarget = yield();
		if (yieldTarget != null) {
			return new ResumeResult(yieldTarget, error);
		}
		else {
			// main coroutine return
			return ResumeResult.mainReturn(error);
		}
	}

}

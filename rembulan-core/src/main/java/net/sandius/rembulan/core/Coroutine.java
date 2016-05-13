package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

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
	protected Cons<ResumeInfo> callStack;

	private Coroutine yieldingTo;
	private Coroutine resuming;

	public Coroutine(Function function) {
		this.callStack = new Cons<>(new ResumeInfo(BootstrapResumable.INSTANCE, Check.notNull(function)));
		this.yieldingTo = null;
		this.resuming = null;
	}

	public boolean isResuming() {
		return resuming != null;
	}

	public boolean isDead() {
		return callStack == null;
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

	public Coroutine resume(Coroutine target) {
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

	public Coroutine yield() {
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

}

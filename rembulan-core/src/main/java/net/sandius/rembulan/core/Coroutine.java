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

	protected Coroutine yieldingTo;
	protected Coroutine resuming;

	public Coroutine(Function function) {
		this.callStack = new Cons<>(new ResumeInfo(BootstrapResumable.INSTANCE, Check.notNull(function)));
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

}

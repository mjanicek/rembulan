package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

public abstract class CoroutineSwitch extends ControlThrowable {

	protected CoroutineSwitch(Cons<ResumeInfo> resumeStack) {
		super(resumeStack);
	}

	public static final class Yield extends CoroutineSwitch {

		public final Object[] args;

		Yield(Cons<ResumeInfo> resumeStack, Object[] args) {
			super(resumeStack);
			this.args = Check.notNull(args);
		}

		public Yield(Object[] args) {
			this(null, args);
		}

		@Override
		public Preemption toPreemption() {
			return new Preemption.CoroutineSwitch.Yield(resumeStack(), args);
		}

	}

	public static final class Resume extends CoroutineSwitch {

		public final Coroutine coroutine;
		public final Object[] args;

		Resume(Cons<ResumeInfo> resumeStack, Coroutine coroutine, Object[] args) {
			super(resumeStack);
			this.coroutine = Check.notNull(coroutine);
			this.args = Check.notNull(args);
		}

		public Resume(Coroutine coroutine, Object[] args) {
			this(null, coroutine, args);
		}

		@Override
		public Preemption toPreemption() {
			return new Preemption.CoroutineSwitch.Resume(resumeStack(), coroutine, args);
		}

	}

}

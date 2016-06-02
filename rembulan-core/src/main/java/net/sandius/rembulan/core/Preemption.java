package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

import java.util.Iterator;

public abstract class Preemption {

	private Cons<ResumeInfo> resumeStack;

	protected Preemption(Cons<ResumeInfo> resumeStack) {
		this.resumeStack = resumeStack;
	}

	public Preemption push(Resumable resumable, Object suspendedState) {
		resumeStack = new Cons<>(new ResumeInfo(resumable, suspendedState), resumeStack);
		return this;
	}

	// LIFO iterator
	public Iterator<ResumeInfo> frames() {
		return Cons.newIterator(resumeStack);
	}

	protected Cons<ResumeInfo> resumeStack() {
		return resumeStack;
	}

	public static class Pause extends Preemption {

		Pause(Cons<ResumeInfo> resumeStack) {
			super(resumeStack);
		}

		public Pause() {
			this(null);
		}

	}

	public abstract static class CoroutineSwitch extends Preemption {

		CoroutineSwitch(Cons<ResumeInfo> resumeStack) {
			super(resumeStack);
		}

		public abstract Object[] arguments();

		public static final class Yield extends CoroutineSwitch {

			private final Object[] args;

			Yield(Cons<ResumeInfo> resumeStack, Object[] args) {
				super(resumeStack);
				this.args = Check.notNull(args);
			}

			public Yield(Object[] args) {
				this(null, args);
			}

			@Override
			public Object[] arguments() {
				return args;
			}

		}

		public static final class Resume extends CoroutineSwitch {

			private final Coroutine coroutine;
			private final Object[] args;

			Resume(Cons<ResumeInfo> resumeStack, Coroutine coroutine, Object[] args) {
				super(resumeStack);
				this.coroutine = Check.notNull(coroutine);
				this.args = Check.notNull(args);
			}

			public Resume(Coroutine coroutine, Object[] args) {
				this(null, coroutine, args);
			}

			public Coroutine target() {
				return coroutine;
			}

			@Override
			public Object[] arguments() {
				return args;
			}

		}

	}

}

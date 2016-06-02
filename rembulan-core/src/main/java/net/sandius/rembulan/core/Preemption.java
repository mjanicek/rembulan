package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.Cons;

import java.util.Iterator;

public abstract class Preemption {

	private Cons<ResumeInfo> resumeStack;

	protected Preemption() {
		this.resumeStack = null;
	}

	public void push(Resumable resumable, Object suspendedState) {
		resumeStack = new Cons<>(new ResumeInfo(resumable, suspendedState), resumeStack);
	}

	// LIFO iterator
	public Iterator<ResumeInfo> frames() {
		return Cons.newIterator(resumeStack);
	}

	public static class Pause extends Preemption {

	}

	public abstract static class CoroutineSwitch extends Preemption {

		public abstract Object[] arguments();

		public static final class Yield extends CoroutineSwitch {

			private final Object[] args;

			public Yield(Object[] args) {
				this.args = Check.notNull(args);
			}

			@Override
			public Object[] arguments() {
				return args;
			}

		}

		public static final class Resume extends CoroutineSwitch {

			private final Coroutine coroutine;
			private final Object[] args;

			public Resume(Coroutine coroutine, Object[] args) {
				this.coroutine = Check.notNull(coroutine);
				this.args = Check.notNull(args);
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

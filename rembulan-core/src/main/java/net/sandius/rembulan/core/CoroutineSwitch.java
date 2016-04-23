package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public abstract class CoroutineSwitch extends ControlThrowable {

	public static final class Yield extends CoroutineSwitch {

		public final Object[] args;

		public Yield(Object[] args) {
			this.args = Check.notNull(args);
		}

	}

	public static final class Resume extends CoroutineSwitch {

		public final Coroutine coroutine;
		public final Object[] args;

		public Resume(Coroutine coroutine, Object[] args) {
			this.coroutine = Check.notNull(coroutine);
			this.args = Check.notNull(args);
		}

	}

}

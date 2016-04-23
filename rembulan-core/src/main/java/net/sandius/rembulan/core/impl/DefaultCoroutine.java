package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.Coroutine;
import net.sandius.rembulan.core.CoroutineFactory;
import net.sandius.rembulan.core.LuaState;

public class DefaultCoroutine extends Coroutine {

	public static final CoroutineFactory FACTORY_INSTANCE = new CoroutineFactory() {
		@Override
		public Coroutine newCoroutine(LuaState state) {
			return new DefaultCoroutine(state);
		}
	};

	public DefaultCoroutine(LuaState state) {
		super(state);
	}

	@Override
	public Status getStatus() {
		return Status.Running;  // FIXME
	}

}

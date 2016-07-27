package net.sandius.rembulan.bm;

import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Coroutine;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.util.Check;

public class DummyExecutionContext implements ExecutionContext {

	private final LuaState state;
	private final ObjectSink objectSink;

	public DummyExecutionContext(LuaState state, ObjectSink objectSink) {
		this.state = Check.notNull(state);
		this.objectSink = Check.notNull(objectSink);
	}

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
		throw new UnsupportedOperationException();
	}

	@Override
	public Coroutine newCoroutine(Function function) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canYield() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void checkPreempt(int cost) throws ControlThrowable {
		throw new UnsupportedOperationException();
	}

}

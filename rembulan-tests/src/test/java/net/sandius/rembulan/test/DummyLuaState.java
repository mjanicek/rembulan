package net.sandius.rembulan.test;

import net.sandius.rembulan.core.Coroutine;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectStack;
import net.sandius.rembulan.core.Table;

public class DummyLuaState extends LuaState {

	private final boolean preempting;
	private final Coroutine mainCoroutine;

	public DummyLuaState(boolean preempting) {
		this.preempting = preempting;
		mainCoroutine = new Coroutine(this, ObjectStack.newEmptyStack(250));
	}

	public static DummyLuaState newDummy(boolean preempting) {
		return new DummyLuaState(preempting);
	}

	@Override
	public Table nilMetatable() {
		return null;
	}

	@Override
	public Table booleanMetatable() {
		return null;
	}

	@Override
	public Table numberMetatable() {
		return null;
	}

	@Override
	public Table stringMetatable() {
		return null;
	}

	@Override
	public Table functionMetatable() {
		return null;
	}

	@Override
	public Table threadMetatable() {
		return null;
	}

	@Override
	public Table lightuserdataMetatable() {
		return null;
	}

	@Override
	public boolean shouldPreemptNow() {
		return preempting;
	}

	@Override
	public Coroutine getCurrentCoroutine() {
		return mainCoroutine;
	}

}

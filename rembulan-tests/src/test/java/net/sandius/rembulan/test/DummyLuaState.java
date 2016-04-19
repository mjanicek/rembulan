package net.sandius.rembulan.test;

import net.sandius.rembulan.core.Coroutine;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.TableFactory;
import net.sandius.rembulan.core.impl.DefaultTable;

public class DummyLuaState extends LuaState {

	private final PreemptionContext preemptionContext;
	private final Coroutine mainCoroutine;

	public DummyLuaState(boolean preempting) {
		this.preemptionContext = preempting
				? PreemptionContext.Always.INSTANCE
				: PreemptionContext.Never.INSTANCE;

		mainCoroutine = new Coroutine(this);
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
	public TableFactory tableFactory() {
		return DefaultTable.FACTORY_INSTANCE;
	}

	@Override
	public PreemptionContext preemptionContext() {
		return preemptionContext;
	}

	@Override
	public Coroutine getCurrentCoroutine() {
		return mainCoroutine;
	}

}

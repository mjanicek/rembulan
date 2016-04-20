package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.CoroutineFactory;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.TableFactory;
import net.sandius.rembulan.core.UpvalueFactory;
import net.sandius.rembulan.util.Check;

public class DefaultLuaState extends LuaState {

	private final UpvalueFactory upvalueFactory;
	private final TableFactory tableFactory;
	private final CoroutineFactory coroutineFactory;

	private final PreemptionContext preemptionContext;

	protected Table nilMetatable;
	protected Table booleanMetatable;
	protected Table numberMetatable;
	protected Table stringMetatable;
	protected Table functionMetatable;
	protected Table threadMetatable;
	protected Table lightuserdataMetatable;

	public DefaultLuaState(UpvalueFactory upvalueFactory, TableFactory tableFactory, CoroutineFactory coroutineFactory, PreemptionContext preemptionContext) {
		this.upvalueFactory = Check.notNull(upvalueFactory);
		this.tableFactory = Check.notNull(tableFactory);
		this.coroutineFactory = Check.notNull(coroutineFactory);
		this.preemptionContext = Check.notNull(preemptionContext);
	}

	public DefaultLuaState(PreemptionContext preemptionContext) {
		this(DefaultUpvalue.FACTORY_INSTANCE,
				DefaultTable.FACTORY_INSTANCE,
				DefaultCoroutine.FACTORY_INSTANCE,
				preemptionContext);
	}

	@Override
	public Table nilMetatable() {
		return nilMetatable;
	}

	@Override
	public Table booleanMetatable() {
		return booleanMetatable;
	}

	@Override
	public Table numberMetatable() {
		return numberMetatable;
	}

	@Override
	public Table stringMetatable() {
		return stringMetatable;
	}

	@Override
	public Table functionMetatable() {
		return functionMetatable;
	}

	@Override
	public Table threadMetatable() {
		return threadMetatable;
	}

	@Override
	public Table lightuserdataMetatable() {
		return lightuserdataMetatable;
	}

	@Override
	public UpvalueFactory upvalueFactory() {
		return upvalueFactory;
	}

	@Override
	public TableFactory tableFactory() {
		return tableFactory;
	}

	@Override
	public CoroutineFactory coroutineFactory() {
		return coroutineFactory;
	}

	@Override
	public PreemptionContext preemptionContext() {
		return preemptionContext;
	}

}

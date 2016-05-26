package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSinkFactory;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.TableFactory;
import net.sandius.rembulan.core.UpvalueFactory;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.GenericBuilder;

public class DefaultLuaState extends LuaState {

	private final ObjectSinkFactory objectSinkFactory;
	private final UpvalueFactory upvalueFactory;
	private final TableFactory tableFactory;

	private final PreemptionContext preemptionContext;

	protected Table nilMetatable;
	protected Table booleanMetatable;
	protected Table numberMetatable;
	protected Table stringMetatable;
	protected Table functionMetatable;
	protected Table threadMetatable;
	protected Table lightuserdataMetatable;

	public DefaultLuaState(ObjectSinkFactory objectSinkFactory,
						   UpvalueFactory upvalueFactory,
						   TableFactory tableFactory,
						   PreemptionContext preemptionContext) {

		this.objectSinkFactory = Check.notNull(objectSinkFactory);
		this.upvalueFactory = Check.notNull(upvalueFactory);
		this.tableFactory = Check.notNull(tableFactory);
		this.preemptionContext = Check.notNull(preemptionContext);
	}

	@Deprecated
	public DefaultLuaState(PreemptionContext preemptionContext) {
		this(CachingObjectSinkFactory.DEFAULT_INSTANCE,
				DefaultUpvalue.FACTORY_INSTANCE,
				DefaultTable.FACTORY_INSTANCE,
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
	public Table setNilMetatable(Table table) {
		Table old = nilMetatable;
		nilMetatable = table;
		return old;
	}

	@Override
	public Table setBooleanMetatable(Table table) {
		Table old = booleanMetatable;
		booleanMetatable = table;
		return old;
	}

	@Override
	public Table setNumberMetatable(Table table) {
		Table old = numberMetatable;
		numberMetatable = table;
		return old;
	}

	@Override
	public Table setStringMetatable(Table table) {
		Table old = stringMetatable;
		stringMetatable = table;
		return old;
	}

	@Override
	public Table setThreadMetatable(Table table) {
		Table old = threadMetatable;
		threadMetatable = table;
		return old;
	}

	@Override
	public Table setLightUserdataMetatable(Table table) {
		Table old = lightuserdataMetatable;
		lightuserdataMetatable = table;
		return old;
	}

	@Override
	public ObjectSinkFactory objectSinkFactory() {
		return objectSinkFactory;
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
	public PreemptionContext preemptionContext() {
		return preemptionContext;
	}

	public static class Builder implements GenericBuilder<DefaultLuaState> {

		private ObjectSinkFactory objectSinkFactory;
		private UpvalueFactory upvalueFactory;
		private TableFactory tableFactory;
		private PreemptionContext preemptionContext;

		protected Builder(ObjectSinkFactory objectSinkFactory,
							   UpvalueFactory upvalueFactory,
							   TableFactory tableFactory,
							   PreemptionContext preemptionContext) {

			this.objectSinkFactory = Check.notNull(objectSinkFactory);
			this.upvalueFactory = Check.notNull(upvalueFactory);
			this.tableFactory = Check.notNull(tableFactory);
			this.preemptionContext = Check.notNull(preemptionContext);
		}

		public Builder() {
			// defaults
			this(CachingObjectSinkFactory.DEFAULT_INSTANCE,
					DefaultUpvalue.FACTORY_INSTANCE,
					DefaultTable.FACTORY_INSTANCE,
					PreemptionContext.Never.INSTANCE);
		}

		public Builder withObjectSinkFactory(ObjectSinkFactory factory) {
			this.objectSinkFactory = Check.notNull(factory);
			return this;
		}

		public Builder withUpvalueFactory(UpvalueFactory factory) {
			this.upvalueFactory = Check.notNull(factory);
			return this;
		}

		public Builder withTableFactory(TableFactory factory) {
			this.tableFactory = Check.notNull(factory);
			return this;
		}

		public Builder withPreemptionContext(PreemptionContext context) {
			this.preemptionContext = Check.notNull(context);
			return this;
		}

		@Override
		public DefaultLuaState build() {
			return new DefaultLuaState(
					objectSinkFactory,
					upvalueFactory,
					tableFactory,
					preemptionContext);
		}
		
	}

}

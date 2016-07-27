package net.sandius.rembulan.core.impl;

import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSinkFactory;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.TableFactory;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.GenericBuilder;

public class DefaultLuaState extends LuaState {

	private final ObjectSinkFactory objectSinkFactory;
	private final TableFactory tableFactory;

	protected Table nilMetatable;
	protected Table booleanMetatable;
	protected Table numberMetatable;
	protected Table stringMetatable;
	protected Table functionMetatable;
	protected Table threadMetatable;
	protected Table lightuserdataMetatable;

	public DefaultLuaState(ObjectSinkFactory objectSinkFactory,
						   TableFactory tableFactory) {

		this.objectSinkFactory = Check.notNull(objectSinkFactory);
		this.tableFactory = Check.notNull(tableFactory);
	}

	public DefaultLuaState() {
		this(CachingObjectSinkFactory.DEFAULT_INSTANCE,
				DefaultTable.FACTORY_INSTANCE);
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
	public Table lightUserdataMetatable() {
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
	public Table setFunctionMetatable(Table table) {
		Table old = functionMetatable;
		functionMetatable = table;
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
	public TableFactory tableFactory() {
		return tableFactory;
	}

	public static class Builder implements GenericBuilder<DefaultLuaState> {

		private ObjectSinkFactory objectSinkFactory;
		private TableFactory tableFactory;

		protected Builder(ObjectSinkFactory objectSinkFactory,
							   TableFactory tableFactory) {

			this.objectSinkFactory = Check.notNull(objectSinkFactory);
			this.tableFactory = Check.notNull(tableFactory);
		}

		public Builder() {
			// defaults
			this(CachingObjectSinkFactory.DEFAULT_INSTANCE,
					DefaultTable.FACTORY_INSTANCE);
		}

		public Builder withObjectSinkFactory(ObjectSinkFactory factory) {
			this.objectSinkFactory = Check.notNull(factory);
			return this;
		}

		public Builder withTableFactory(TableFactory factory) {
			this.tableFactory = Check.notNull(factory);
			return this;
		}

		@Override
		public DefaultLuaState build() {
			return new DefaultLuaState(
					objectSinkFactory,
					tableFactory);
		}
		
	}

}

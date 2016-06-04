package net.sandius.rembulan.lib;

import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.MetatableProvider;
import net.sandius.rembulan.core.Metatables;
import net.sandius.rembulan.core.PlainValueTypeNamer;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.ValueTypeNamer;
import net.sandius.rembulan.util.Check;

public class LibUtils {

	public static final String MT_NAME = "__name";

	public static Table init(LuaState state, Lib lib) {
		Check.notNull(state);
		Table env = state.newTable();
		lib.installInto(state, env);
		return env;
	}

	public static void setIfNonNull(Table table, String key, Object value) {
		Check.notNull(table);
		if (value != null) {
			table.rawset(key, value);
		}
	}

	public static class NameMetamethodValueTypeNamer implements ValueTypeNamer {

		private final MetatableProvider metatableProvider;

		public NameMetamethodValueTypeNamer(MetatableProvider metatableProvider) {
			this.metatableProvider = Check.notNull(metatableProvider);
		}

		@Override
		public String typeNameOf(Object instance) {
			Object nameField = Metatables.getMetamethod(metatableProvider, MT_NAME, instance);
			if (nameField instanceof String) {
				return (String) nameField;
			}
			else {
				return PlainValueTypeNamer.INSTANCE.typeNameOf(instance);
			}
		}

	}

}

package net.sandius.rembulan.lib;

import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Table;

public interface Lib {

	void installInto(LuaState state, Table env);

}

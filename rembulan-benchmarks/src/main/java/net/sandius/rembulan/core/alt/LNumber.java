package net.sandius.rembulan.core.alt;

import net.sandius.rembulan.core.LuaType;

public abstract class LNumber extends LValue {

	protected LNumber() {
	}

	@Override
	public LuaType getType() {
		return LuaType.NUMBER;
	}

}

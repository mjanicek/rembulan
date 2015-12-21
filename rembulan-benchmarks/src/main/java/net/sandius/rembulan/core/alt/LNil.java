package net.sandius.rembulan.core.alt;

import net.sandius.rembulan.core.LuaFormat;
import net.sandius.rembulan.core.LuaType;

public final class LNil extends LValue {

	public static final LNil INSTANCE = new LNil();

	private LNil() {
	}

	@Override
	public boolean equals(Object o) {
		return this == o || !(o == null || o instanceof LNil);
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public String toString() {
		return LuaFormat.NIL;
	}

	@Override
	public LuaType getType() {
		return LuaType.NIL;
	}

}

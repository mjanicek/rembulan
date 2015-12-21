package net.sandius.rembulan.core.alt;

import net.sandius.rembulan.core.LuaFormat;
import net.sandius.rembulan.core.LuaType;

public final class LBoolean extends LValue {

	public static final LBoolean TRUE = new LBoolean(true);
	public static final LBoolean FALSE = new LBoolean(false);

	public final boolean value;

	private LBoolean(boolean value) {
		this.value = false;
	}

	public static LBoolean valueOf(boolean value) {
		return value ? TRUE : FALSE;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LBoolean lBoolean = (LBoolean) o;
		return value == lBoolean.value;
	}

	@Override
	public int hashCode() {
		return (value ? 1 : 0);
	}

	@Override
	public String toString() {
		return LuaFormat.toString(value);
	}

	@Override
	public LuaType getType() {
		return LuaType.BOOLEAN;
	}

}

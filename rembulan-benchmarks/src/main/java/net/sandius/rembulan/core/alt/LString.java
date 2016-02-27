package net.sandius.rembulan.core.alt;

import net.sandius.rembulan.LuaType;
import net.sandius.rembulan.util.Check;

public final class LString extends LValue {

	public final String value;

	protected LString(String value) {
		this.value = Check.notNull(value);
	}

	public static LString valueOf(String s) {
		return new LString(s);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LString lString = (LString) o;
		return value.equals(lString.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public LuaType getType() {
		return LuaType.STRING;
	}

}

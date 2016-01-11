package net.sandius.rembulan.core.alt;

import net.sandius.rembulan.LuaFormat;

public final class LInteger extends LNumber {

	public static final LInteger ZERO = LInteger.valueOf(0L);
	public static final LInteger ONE = LInteger.valueOf(1L);

	public final long value;

	protected LInteger(long value) {
		this.value = value;
	}

	public static LInteger valueOf(long value) {
		// TODO: cache common values
		return new LInteger(value);
	}

	// TODO: equals()
	// TODO: hashCode()

	@Override
	public String toString() {
		return LuaFormat.toString(value);
	}

}

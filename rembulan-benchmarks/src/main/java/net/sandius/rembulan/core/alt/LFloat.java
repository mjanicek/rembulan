package net.sandius.rembulan.core.alt;

import net.sandius.rembulan.LuaFormat;

public final class LFloat extends LNumber {

	public static final LFloat ZERO = LFloat.valueOf(0L);
	public static final LFloat ONE = LFloat.valueOf(1L);

	public final double value;

	protected LFloat(double value) {
		this.value = value;
	}

	public static LFloat valueOf(double value) {
		return new LFloat(value);
	}

	// TODO: equals()
	// TODO: hashCode()

	@Override
	public String toString() {
		return LuaFormat.toString(value);
	}

}

package net.sandius.rembulan.core;

public abstract class Function {

	@Override
	public String toString() {
		return "function: 0x" + Integer.toHexString(hashCode());
	}

}

package net.sandius.rembulan.core;

public abstract class Function implements Func {

	@Override
	public String toString() {
		return "function: 0x" + Integer.toHexString(hashCode());
	}

}

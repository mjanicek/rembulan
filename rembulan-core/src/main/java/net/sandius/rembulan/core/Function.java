package net.sandius.rembulan.core;

public abstract class Function implements Invokable, Resumable {

	@Override
	public String toString() {
		return "function: 0x" + Integer.toHexString(hashCode());
	}

}

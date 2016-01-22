package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.Prototype;

public abstract class NNode {

	public NNode() {
	}

	protected abstract String selfToString();

	protected abstract String nextToString();

	@Override
	public String toString() {
		return selfToString() + " -> " + nextToString();
	}

	public Slots registerEffect(Prototype proto, Slots slots) {
		return slots;
	}

}

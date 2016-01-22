package net.sandius.rembulan.compiler.gen;

public abstract class NTerminal extends NNode {

	public NTerminal() {
		super();
	}

	@Override
	public final String nextToString() {
		return null;
	}

	@Override
	public String toString() {
		return selfToString();
	}

}

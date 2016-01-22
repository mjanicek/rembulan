package net.sandius.rembulan.compiler.gen;

import java.util.Collections;

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

	@Override
	public final Iterable<NNode> out() {
		return Collections.emptySet();
	}

	@Override
	public final int outDegree() {
		return 0;
	}

	@Override
	public final void replaceOutgoing(NNode n, NNode replacement) {
		throw new UnsupportedOperationException();
	}

}

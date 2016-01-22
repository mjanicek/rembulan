package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

import java.util.Collections;

public abstract class NSingleInput extends NNode {

	private NNode prev;

	public NSingleInput() {
		super();
		this.prev = null;
	}

	public NNode prev() {
		return prev;
	}

	@Override
	public Iterable<NNode> in() {
		if (prev != null) {
			return Collections.singleton(prev);
		}
		else {
			return Collections.emptySet();
		}
	}

	@Override
	public int inDegree() {
		return prev != null ? 1 : 0;
	}

	@Override
	public void detachIncoming(NNode n) {
		Check.isEq(n, prev);
		prev = null;
	}

	@Override
	public void attachIncoming(NNode n) {
		prev = n;
	}

}

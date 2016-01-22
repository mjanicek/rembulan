package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

import java.util.Collections;

public final class NEntry extends NNode {

	private NNode next;

	public NEntry() {
		super();
		this.next = null;
	}

	@Override
	protected String selfToString() {
		return "ENTRY";
	}

	@Override
	protected String nextToString() {
		return next != null ? next.toString() : "NULL";
	}

	public NNode next() {
		return next;
	}

	@Override
	public Iterable<NNode> in() {
		return Collections.emptySet();
	}

	@Override
	public int inDegree() {
		return 0;
	}

	@Override
	public void attachIncoming(NNode n) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void detachIncoming(NNode n) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<NNode> out() {
		if (next != null) {
			return Collections.singleton(next);
		}
		else {
			return Collections.emptySet();
		}
	}

	@Override
	public int outDegree() {
		return next != null ? 1 : 0;
	}

	@Override
	public void replaceOutgoing(NNode n, NNode replacement) {
		Check.notNull(n);
		Check.isEq(n, next);

		next = replacement;
	}

	public NEntry enter(NNode n) {
		Check.notNull(n);

		if (next != null) {
			next.detachIncoming(this);
		}

		next = n;
		n.attachIncoming(this);

		return this;
	}

}

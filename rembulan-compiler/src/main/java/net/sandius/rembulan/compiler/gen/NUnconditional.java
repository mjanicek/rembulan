package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

import java.util.Collections;

public abstract class NUnconditional extends NNode {

	private NNode next;

	public NUnconditional(NNode next) {
		super();
		this.next = next;
	}

	public NUnconditional() {
		this(null);
	}

	@Override
	public String nextToString() {
		return next != null ? next.toString() : "NULL";
	}

	@Override
	public final Iterable<NNode> out() {
		return Collections.singleton(next);
	}

	@Override
	public final int outDegree() {
		return next != null ? 1 : 0;
	}

	@Override
	public final void replaceOutgoing(NNode n, NNode replacement) {
		Check.notNull(n);
		Check.isEq(n, next);

		next = replacement;
	}

	public NNode next() {
		return next;
	}

	public NUnconditional followedBy(NNode n) {
		Check.notNull(n);

		if (next != null) {
			// detach from old next
			next.detachIncoming(this);
		}

		next = n;
		n.attachIncoming(this);

		return this;
	}

	public void remove() {
		Check.isEq(inDegree(), 1);
		Check.notNull(next);

		NNode nxt = next;
		next.detachIncoming(this);
		next = null;

		for (NNode n : in()) {
			n.replaceOutgoing(this, nxt);
			nxt.attachIncoming(n);
		}

	}


}

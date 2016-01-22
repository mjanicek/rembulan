package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class NEntry extends NNode {

	private NLabel next;

	public NEntry() {
		super();
		this.next = null;
	}

	@Override
	protected String selfToString() {
		return "ENTRY";
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
			Set<NNode> result = new HashSet<>();
			result.add(next);
			return result;
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
		if (!(replacement instanceof NLabel)) {
			throw new IllegalArgumentException("Replacement is not a label");
		}

		next = (NLabel) replacement;
	}

	public NEntry enter(NLabel n) {
		Check.notNull(n);

		if (next != null) {
			next.detachIncoming(this);
		}

		next = n;
		n.attachIncoming(this);

		return this;
	}

	public NEntry enter(NNode n) {
		return enter(NLabel.guard(n));
	}

}

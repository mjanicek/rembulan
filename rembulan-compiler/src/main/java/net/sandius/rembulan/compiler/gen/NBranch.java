package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

import java.util.HashSet;
import java.util.Set;

public abstract class NBranch extends NNode {

	private NLabel trueBranch;
	private NLabel falseBranch;

	public NBranch() {
		super();
		this.trueBranch = null;
		this.falseBranch = null;
	}

	@Override
	public String nextToString() {
		return "true:("
				+ (trueBranch != null ? trueBranch.selfToString() : "NULL")
				+ "), false:("
				+ (falseBranch != null ? falseBranch.selfToString() : "NULL")
				+ ")";
	}

	@Override
	public final Iterable<NNode> out() {
		Set<NNode> result = new HashSet<>();
		if (trueBranch != null) result.add(trueBranch);
		if (falseBranch != null) result.add(falseBranch);
		return result;
	}

	@Override
	public final int outDegree() {
		return (trueBranch != null ? 1 : 0) + (falseBranch != null ? 1 : 0);
	}

	@Override
	public final void replaceOutgoing(NNode n, NNode replacement) {
		Check.notNull(n);
		if (!(replacement instanceof NLabel)) {
			throw new IllegalArgumentException("Replacement is not a label");
		}

		NLabel rep = (NLabel) replacement;

		if (n == trueBranch || n == falseBranch) {
			if (n == trueBranch) trueBranch = rep;
			if (n == falseBranch) falseBranch = rep;
		}
		else {
			throw new IllegalArgumentException("Node not a branch: " + n.toString());
		}
	}

	public NLabel trueBranch() {
		return trueBranch;
	}

	public NLabel falseBranch() {
		return falseBranch;
	}

	public NBranch withTrueBranch(NLabel n) {
		Check.notNull(n);

		if (trueBranch != null) {
			trueBranch.detachIncoming(this);
		}

		trueBranch = n;
		n.attachIncoming(this);

		return this;
	}

	public NBranch withFalseBranch(NLabel n) {
		Check.notNull(n);

		if (falseBranch != null) {
			falseBranch.detachIncoming(this);
		}

		falseBranch = n;
		n.attachIncoming(this);

		return this;
	}

}

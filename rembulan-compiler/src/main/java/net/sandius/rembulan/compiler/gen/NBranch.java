package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

import java.util.HashSet;
import java.util.Set;

public abstract class NBranch extends NNode {

	private NNode trueBranch;
	private NNode falseBranch;

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

		if (n == trueBranch || n == falseBranch) {
			if (n == trueBranch) trueBranch = replacement;
			if (n == falseBranch) falseBranch = replacement;
		}
		else {
			throw new IllegalArgumentException("Node not a branch: " + n.toString());
		}
	}

	public NNode trueBranch() {
		return trueBranch;
	}

	public NNode falseBranch() {
		return falseBranch;
	}

	public NBranch withTrueBranch(NNode n) {
		Check.notNull(n);

		if (trueBranch != null) {
			trueBranch.detachIncoming(this);
		}

		trueBranch = n;
		n.attachIncoming(this);

		return this;
	}

	public NBranch withFalseBranch(NNode n) {
		Check.notNull(n);

		if (falseBranch != null) {
			falseBranch.detachIncoming(this);
		}

		falseBranch = n;
		n.attachIncoming(this);

		return this;
	}

}

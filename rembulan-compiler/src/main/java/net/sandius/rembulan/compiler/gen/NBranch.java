package net.sandius.rembulan.compiler.gen;

public abstract class NBranch extends NNode {

	public NNode trueBranch;
	public NNode falseBranch;

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

}

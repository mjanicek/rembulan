package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.util.Check;

public class Entry implements Node, Jump {

	public final String name;
	private Target target;

	public Entry(String name, Target target) {
		Check.notNull(target);
		this.name = name;
		this.target = target;
	}

	public Entry(Target target) {
		this(null, target);
	}

	@Override
	public String toString() {
		return "Entry:" + (name != null ? name : Integer.toHexString(System.identityHashCode(this))) + "";
	}

	public Target target() {
		return target;
	}

	public void setTarget(Target target) {
		Check.notNull(target);
		this.target.dec(this);
		target.inc(this);
		this.target = target;
	}

	@Override
	public void accept(NodeVisitor visitor) {
		if (visitor.visitNode(this)) {
			visitor.visitEdge(this, target);
			target.accept(visitor);
		}
	}

}

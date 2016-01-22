package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.util.Check;

public class Entry implements Node, Jump {

	private Target target;

	public Entry(Target target) {
		Check.notNull(target);
		this.target = target;
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
		if (visitor.visit(this)) {
			target.accept(visitor);
		}
	}

}

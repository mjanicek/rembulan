package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.util.Check;

public class UnconditionalJump implements Node, Sink, Jump {

	private Src prev;
	private Target target;

	public UnconditionalJump(Target target) {
		Check.notNull(target);
		this.prev = Nodes.DUMMY_SRC;
		this.target = target;
		target.inc(this);
	}

	@Override
	public String toString() {
		return "Jump-To(" + target.toString() + ")";
	}

	@Override
	public Src prev() {
		return prev;
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
	public void setPrev(Src to) {
		Check.notNull(to);
		this.prev = to;
	}

	@Override
	public void prependSource(Src that) {
		Check.notNull(that);
		that.setNext(this);
		this.setPrev(that);
	}

	@Override
	public void accept(NodeVisitor visitor) {
		if (visitor.visitNode(this)) {
			visitor.visitEdge(this, target);
			target.accept(visitor);
		}
	}

}

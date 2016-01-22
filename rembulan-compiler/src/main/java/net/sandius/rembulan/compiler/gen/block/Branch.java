package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.util.Check;

public class Branch implements Node, Sink, Jump {

	private Src prev;
	private Target trueBranch;
	private Target falseBranch;

	public Branch(Target trueBranch, Target falseBranch) {
		Check.notNull(trueBranch);
		Check.notNull(falseBranch);

		this.prev = Nodes.DUMMY_SRC;
		this.trueBranch = trueBranch;
		trueBranch.inc(this);
		this.falseBranch = falseBranch;
		falseBranch.inc(this);
	}

	@Override
	public Src prev() {
		return prev;
	}

	@Override
	public void setPrev(Src to) {
		Check.notNull(to);
		this.prev = to;
	}

	public Target trueBranch() {
		return trueBranch;
	}

	public Target falseBranch() {
		return falseBranch;
	}

	public void setTrueBranch(Target target) {
		Check.notNull(target);
		trueBranch.dec(this);
		target.inc(this);
		trueBranch = target;
	}

	public void setFalseBranch(Target target) {
		Check.notNull(target);
		falseBranch.dec(this);
		target.inc(this);
		falseBranch = target;
	}

	@Override
	public void prependSource(Src that) {
		Check.notNull(that);
		this.setPrev(that);
		that.setNext(this);
	}

	@Override
	public void accept(NodeVisitor visitor) {
		if (visitor.visit(this)) {
			trueBranch.accept(visitor);
			falseBranch.accept(visitor);
		}
	}

}


package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.util.Check;

public abstract class Branch implements Node, Sink, Jump {

	private Slots inSlots;

	private Src prev;
	private Target trueBranch;
	private Target falseBranch;

	public Branch(Target trueBranch, Target falseBranch) {
		Check.notNull(trueBranch);
		Check.notNull(falseBranch);

		this.inSlots = null;
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
		if (visitor.visitNode(this)) {
			visitor.visitEdge(this, trueBranch);
			trueBranch.accept(visitor);
			visitor.visitEdge(this, falseBranch);
			falseBranch.accept(visitor);
		}
	}

	@Override
	public Slots effect(Slots in) {
		return in;
	}

	@Override
	public Slots inSlots() {
		return inSlots;
	}

	@Override
	public boolean pushSlots(Slots s) {
		Check.notNull(s);
		if (!s.equals(inSlots)) {
			inSlots = s;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public void clearSlots() {
		inSlots = null;
	}

}


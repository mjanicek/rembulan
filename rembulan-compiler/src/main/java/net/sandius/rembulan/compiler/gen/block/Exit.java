package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.util.Check;

public abstract class Exit implements Node, Sink {

	private Slots inSlots;

	private Src prev;

	public Exit() {
		this.inSlots = null;
		this.prev = Nodes.DUMMY_SRC;
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

	@Override
	public void prependSource(Src that) {
		Check.notNull(that);
		this.setPrev(that);
		that.setNext(this);
	}

	@Override
	public void accept(NodeVisitor visitor) {
		visitor.visitNode(this);
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

package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.util.Check;

public abstract class Exit implements Node, Sink {

	private Src prev;

	public Exit() {
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
		throw new UnsupportedOperationException();
	}

}

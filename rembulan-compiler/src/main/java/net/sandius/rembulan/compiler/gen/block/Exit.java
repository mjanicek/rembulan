package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.util.Check;

public class Exit implements Node, Sink {

	private Src prev;

	public Exit(Src prev) {
		Check.notNull(prev);
		this.prev = prev;
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
		visitor.visit(this);
	}

}

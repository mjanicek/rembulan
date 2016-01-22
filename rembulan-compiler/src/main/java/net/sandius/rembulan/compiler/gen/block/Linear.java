package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.util.Check;

public class Linear implements Node, Sink, Src {

	private Src prev;
	private Sink next;

	public Linear() {
		this.prev = Nodes.DUMMY_SRC;
		this.next = Nodes.DUMMY_SINK;
	}

	@Override
	public Src prev() {
		return prev;
	}

	@Override
	public void prependSource(Src that) {
		Check.notNull(that);
		this.setPrev(that);
		that.setNext(this);
	}

	@Override
	public void appendSink(Sink that) {
		Check.notNull(that);
		this.setNext(that);
		that.setPrev(this);
	}

	@Override
	public Sink next() {
		return next;
	}

	@Override
	public void setNext(Sink n) {
		Check.notNull(n);
		this.next = n;
	}

	@Override
	public void setPrev(Src n) {
		Check.notNull(n);
		this.prev = n;
	}

	@Override
	public void accept(NodeVisitor visitor) {
		if (visitor.visit(this)) {
			next.accept(visitor);
		}
	}

}

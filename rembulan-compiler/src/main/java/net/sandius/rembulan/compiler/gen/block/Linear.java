package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.CodeVisitor;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.util.Check;

public abstract class Linear implements Node, Sink, Src {

	private SlotState inSlots;

	private Src prev;
	private Sink next;

	public Linear() {
		this.inSlots = null;
		this.prev = null;
		this.next = null;
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
	public Src appendLinear(Linear that) {
		Check.notNull(that);
		appendSink(that);
		return that;
	}

	public void insertAfter(Src n) {
		Sink next = n.next();
		n.appendSink(this);
		next.prependSource(this);
	}

	public void insertBefore(Sink n) {
		Src prev = n.prev();
		prev.appendSink(this);
		n.prependSource(this);
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

	public void remove() {
		Src p = this.prev();
		Sink n = this.next();

		p.setNext(n);
		n.setPrev(p);

		this.prev = null;
		this.next = null;
	}

	public void replaceWith(Linear replacement) {
		Src p = this.prev();
		Sink n = this.next();

		p.setNext(replacement);
		replacement.setPrev(p);
		replacement.setNext(n);
		n.setPrev(replacement);

		this.prev = null;
		this.next = null;
	}

	@Override
	public void accept(NodeVisitor visitor) {
		if (visitor.visitNode(this)) {
			visitor.visitEdge(this, next);
			next.accept(visitor);
		}
	}

	protected SlotState effect(SlotState in) {
		return in;
	}

	@Override
	public SlotState inSlots() {
		return inSlots;
	}

	@Override
	public SlotState outSlots() {
		return effect(inSlots());
	}

	@Override
	public boolean pushSlots(SlotState s) {
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

	@Override
	public void emit(CodeVisitor visitor) {
		visitor._missing(this);
	}

}

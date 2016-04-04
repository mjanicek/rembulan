package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.ReturnType;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.util.Check;

public abstract class Exit implements Node, Sink {

	private SlotState inSlots;

	private Src prev;

	public Exit() {
		this.inSlots = null;
		this.prev = null;
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
	public SlotState inSlots() {
		return inSlots;
	}

	@Override
	public SlotState outSlots() {
		return null;
	}

	public abstract ReturnType returnType();

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
	public void emit(Emit e) {
		e._note("TODO: emit " + getClass().getName() + " for " + this.toString());
	}

}

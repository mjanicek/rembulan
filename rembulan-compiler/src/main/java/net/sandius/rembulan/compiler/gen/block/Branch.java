package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.util.Check;

public abstract class Branch implements Node, Sink, Jump {

	private SlotState inSlots;

	private Src prev;
	private Target trueBranch;
	private Target falseBranch;

	public Branch(Target trueBranch, Target falseBranch) {
		this.inSlots = null;
		this.prev = null;

		this.trueBranch = Check.notNull(trueBranch);
		this.falseBranch = Check.notNull(falseBranch);

		trueBranch.inc(this);
		falseBranch.inc(this);
	}

	@Override
	public Src prev() {
		return prev;
	}

	@Override
	public void setPrev(Src to) {
		this.prev = Check.notNull(to);
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
	public SlotState inSlots() {
		return inSlots;
	}

	@Override
	public SlotState outSlots() {
		return inSlots();
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

	public void inline(boolean branch) {
		Target target = branch ? trueBranch() : falseBranch();

		// disconnect both branches
		trueBranch().dec(this);
		falseBranch().dec(this);

		UnconditionalJump jmp = new UnconditionalJump(target);
		this.prev().appendSink(jmp);

		jmp.tryInlining();
	}

	public InlineTarget canBeInlined() {
		return InlineTarget.CANNOT_BE_INLINED;
	}

	public enum InlineTarget {
		CANNOT_BE_INLINED,
		TRUE_BRANCH,
		FALSE_BRANCH
	}

	@Override
	public void emit(CodeEmitter e) {
		e._note("TODO: emit " + getClass().getName() + " for " + this.toString());
	}

}


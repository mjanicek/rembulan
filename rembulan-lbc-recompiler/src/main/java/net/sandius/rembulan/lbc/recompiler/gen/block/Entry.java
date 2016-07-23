package net.sandius.rembulan.lbc.recompiler.gen.block;

import net.sandius.rembulan.compiler.analysis.types.TypeSeq;
import net.sandius.rembulan.lbc.recompiler.gen.CodeVisitor;
import net.sandius.rembulan.lbc.recompiler.gen.SlotState;
import net.sandius.rembulan.util.Check;

public class Entry implements Node, Jump {

	public final String name;
	private final TypeSeq typeSeq;
	private final int slotSize;

	private Target target;

	public Entry(String name, TypeSeq fixedParameters, int slotSize, Target target) {
		this.name = name;
		this.typeSeq = Check.notNull(fixedParameters);
		this.slotSize = Check.nonNegative(slotSize);
		this.target = Check.notNull(target);
	}

	public Entry(TypeSeq typeSeq, int slotSize, Target target) {
		this(null, typeSeq, slotSize, target);
	}

	@Override
	public String toString() {
		return "Entry:" + (name != null ? name : Integer.toHexString(System.identityHashCode(this))) + "";
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
	public void accept(NodeVisitor visitor) {
		if (visitor.visitNode(this)) {
			visitor.visitEdge(this, target);
			target.accept(visitor);
		}
	}

	public TypeSeq arguments() {
		return typeSeq;
	}

	@Override
	public SlotState inSlots() {
		return null;
	}

	@Override
	public SlotState outSlots() {
		return SlotState.fromFixedTypes(typeSeq, slotSize);
	}

	@Override
	public boolean pushSlots(SlotState s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearSlots() {
		// no-op
	}

	@Override
	public void emit(CodeVisitor visitor) {
		visitor._ignored(this);
	}

}

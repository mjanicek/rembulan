package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.util.Check;

public class Entry implements Node, Jump {

	public final String name;
	private final Slots entrySlots;

	private Target target;

	public Entry(String name, Slots entrySlots, Target target) {
		Check.notNull(target);
		Check.notNull(entrySlots);

		this.name = name;
		this.entrySlots = entrySlots;
		this.target = target;
	}

	public Entry(Slots entrySlots, Target target) {
		this(null, entrySlots, target);
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

	@Override
	public Slots inSlots() {
		return null;
	}

	@Override
	public Slots outSlots() {
		return entrySlots;
	}

	@Override
	public boolean pushSlots(Slots s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearSlots() {
		// no-op
	}

}

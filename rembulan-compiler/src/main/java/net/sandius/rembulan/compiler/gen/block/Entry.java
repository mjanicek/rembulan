package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.ArgTypes;
import net.sandius.rembulan.compiler.gen.Slots;
import net.sandius.rembulan.util.Check;

public class Entry implements Node, Jump {

	public final String name;
	private final ArgTypes argTypes;
	private final int slotSize;

	private Target target;

	public Entry(String name, ArgTypes argTypes, int slotSize, Target target) {
		Check.notNull(target);
		Check.notNull(argTypes);

		this.name = name;
		this.argTypes = argTypes;
		this.slotSize = slotSize;
		this.target = target;
	}

	public Entry(ArgTypes argTypes, int slotSize, Target target) {
		this(null, argTypes, slotSize, target);
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

	public ArgTypes arguments() {
		return argTypes;
	}

	@Override
	public Slots inSlots() {
		return null;
	}

	@Override
	public Slots outSlots() {
		return argTypes.toSlots(slotSize);
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

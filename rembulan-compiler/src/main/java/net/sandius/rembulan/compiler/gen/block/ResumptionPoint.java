package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;

public class ResumptionPoint extends Linear {

	@Override
	public String toString() {
		return "Resume::" + Integer.toHexString(hashCode());
	}

	@Override
	protected Slots effect(Slots in) {
		return in;
	}

}

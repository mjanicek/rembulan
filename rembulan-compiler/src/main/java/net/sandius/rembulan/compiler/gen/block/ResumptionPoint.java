package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.SlotState;

public class ResumptionPoint extends Linear {

	@Override
	public String toString() {
		return "Resume::" + Integer.toHexString(hashCode());
	}

	@Override
	protected SlotState effect(SlotState in) {
		return in;
	}

}

package net.sandius.rembulan.lbc.recompiler.gen.block;

import net.sandius.rembulan.lbc.recompiler.gen.SlotState;

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

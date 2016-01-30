package net.sandius.rembulan.compiler.gen.block;

public class ResumptionPoint extends Linear {

	@Override
	public String toString() {
		return "Resume::" + Integer.toHexString(hashCode());
	}

}

package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.util.Check;

public class CloseUpvalues extends Linear {

	public final int fromIndex;

	public CloseUpvalues(int fromIndex) {
		Check.nonNegative(fromIndex);
		this.fromIndex = fromIndex;
	}

	@Override
	public String toString() {
		return "CloseUpvalues(" + fromIndex + ")";
	}

}

package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

public class NCloseUpvalues extends NUnconditional {

	public final int fromIndex;

	public NCloseUpvalues(int fromIndex) {
		Check.nonNegative(fromIndex);
		this.fromIndex = fromIndex;
	}

	@Override
	public String selfToString() {
		return "CloseUpvalues(" + fromIndex + ")";
	}

}

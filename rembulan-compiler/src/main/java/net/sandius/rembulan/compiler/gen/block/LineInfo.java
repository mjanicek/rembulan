package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;

public class LineInfo extends Linear {

	public final int line;

	public LineInfo(int line) {
		this.line = line;
	}

	@Override
	public String toString() {
		return "Line(" + line + ")";
	}

	@Override
	protected Slots effect(Slots in) {
		return in;
	}

}

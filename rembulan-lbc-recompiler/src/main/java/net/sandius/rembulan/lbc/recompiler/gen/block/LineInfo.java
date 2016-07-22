package net.sandius.rembulan.lbc.recompiler.gen.block;

import net.sandius.rembulan.lbc.recompiler.gen.CodeVisitor;

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
	public void emit(CodeVisitor visitor) {
		visitor._ignored(this);
	}

}

package net.sandius.rembulan.compiler.gen.block;

public class LineInfo extends Linear {

	public final int line;

	public LineInfo(int line) {
		this.line = line;
	}

	@Override
	public String toString() {
		return "Line(" + line + ")";
	}

}

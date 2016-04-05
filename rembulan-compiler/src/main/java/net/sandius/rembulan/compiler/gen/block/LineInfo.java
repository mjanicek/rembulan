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

	@Override
	public void emit(CodeEmitter e) {
		e._line_here(line);
	}

}

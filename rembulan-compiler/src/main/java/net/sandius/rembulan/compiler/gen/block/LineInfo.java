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
	public void emit(Emit e) {
		String labelName = e._asLabel(this);
		System.out.println(labelName);
		System.out.println("  LINEINFO " + line + " " + labelName);
	}

}

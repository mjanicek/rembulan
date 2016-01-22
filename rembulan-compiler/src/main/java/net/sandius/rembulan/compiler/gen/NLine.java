package net.sandius.rembulan.compiler.gen;

public class NLine extends NUnconditional {

	public final int line;

	public NLine(int line) {
		super();
		this.line = line;
	}

	@Override
	public String selfToString() {
		return "AtLine(" + line + ")";
	}

}

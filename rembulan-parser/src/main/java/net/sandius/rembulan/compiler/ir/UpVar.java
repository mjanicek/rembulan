package net.sandius.rembulan.compiler.ir;

public class UpVar {

	private final int idx;

	public UpVar(int idx) {
		this.idx = idx;
	}

	@Override
	public String toString() {
		return "^" + idx;
	}

}

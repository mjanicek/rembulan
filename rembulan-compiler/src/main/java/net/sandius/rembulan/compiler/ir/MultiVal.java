package net.sandius.rembulan.compiler.ir;

public class MultiVal {

	private final int idx;

	public MultiVal(int idx) {
		this.idx = idx;
	}

	@Override
	public String toString() {
		return "*" + idx;
	}

}

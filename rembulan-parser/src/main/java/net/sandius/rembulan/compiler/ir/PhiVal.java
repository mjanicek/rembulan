package net.sandius.rembulan.compiler.ir;

public class PhiVal extends AbstractVal {

	private final int idx;

	public PhiVal(int idx) {
		this.idx = idx;
	}

	@Override
	public String toString() {
		return "&" + idx;
	}

}

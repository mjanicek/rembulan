package net.sandius.rembulan.compiler.ir;

public class Var extends AbstractVar {

	private final int idx;

	public Var(int idx) {
		this.idx = idx;
	}

	@Override
	public String toString() {
		return "$" + idx;
	}

}

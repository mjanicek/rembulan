package net.sandius.rembulan.compiler.ir;

public class Val extends AbstractVal {

	private final int idx;

	public Val(int idx) {
		this.idx = idx;
	}

	@Override
	public String toString() {
		return ":" + idx;
	}

}

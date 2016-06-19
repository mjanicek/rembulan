package net.sandius.rembulan.compiler.ir;

public class Temp {

	private final int idx;

	public Temp(int idx) {
		this.idx = idx;
	}

	@Override
	public String toString() {
		return ":" + idx;
	}

}

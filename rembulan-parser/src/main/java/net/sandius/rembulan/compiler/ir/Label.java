package net.sandius.rembulan.compiler.ir;

public class Label {

	private final int idx;

	public Label(int idx) {
		this.idx = idx;
	}

	@Override
	public String toString() {
		return "L" + idx;
	}

	public int idx() {
		return idx;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

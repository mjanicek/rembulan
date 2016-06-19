package net.sandius.rembulan.compiler.ir;

public class TabSet extends IRNode {

	public TabSet() {

	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

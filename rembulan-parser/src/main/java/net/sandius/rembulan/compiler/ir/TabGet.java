package net.sandius.rembulan.compiler.ir;

public class TabGet extends IRNode {

	public TabGet() {
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

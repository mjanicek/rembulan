package net.sandius.rembulan.compiler.ir;

public class Vararg extends IRNode {

	public Vararg() {
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

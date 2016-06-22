package net.sandius.rembulan.compiler.ir;

@Deprecated
public class Dup extends IRNode {

	public Dup() {
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

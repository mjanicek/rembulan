package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

@Deprecated
public class ArrayGet extends IRNode {

	private final int index;

	public ArrayGet(int index) {
		this.index = Check.nonNegative(index);
	}

	public int index() {
		return index;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

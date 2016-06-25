package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class CPUWithdraw extends IRNode {

	private final int cost;

	public CPUWithdraw(int cost) {
		this.cost = Check.positive(cost);
	}

	public int cost() {
		return cost;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

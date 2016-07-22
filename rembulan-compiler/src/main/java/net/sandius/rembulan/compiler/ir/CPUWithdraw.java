package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

import java.util.Objects;

public class CPUWithdraw extends BodyNode {

	private final int cost;

	public CPUWithdraw(int cost) {
		this.cost = Check.positive(cost);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CPUWithdraw that = (CPUWithdraw) o;
		return this.cost == that.cost;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cost);
	}

	public int cost() {
		return cost;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

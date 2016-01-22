package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

public class NAccount extends NUnconditional {

	public final int cost;

	public NAccount(int cost) {
		super();
		Check.positive(cost);

		this.cost = cost;
	}

	@Override
	public String selfToString() {
		return "Account(" + cost + ")";
	}

}

package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.Slots;

public abstract class AccountingNode extends Linear {

	@Override
	protected Slots effect(Slots in) {
		return in;
	}

	public static class Sum extends AccountingNode {

		public final int cost;

		public Sum(int cost) {
			this.cost = cost;
		}

		@Override
		public String toString() {
			return "CPU-Sum(" + cost + ")";
		}

	}

	public static class Tick extends AccountingNode {

		@Override
		public String toString() {
			return "CPU-Tick";
		}

	}

	public static class End extends AccountingNode {

		@Override
		public String toString() {
			return "CPU-Accounting-End";
		}

	}

}

package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.compiler.gen.CodeVisitor;

public abstract class AccountingNode extends Linear {

	@Override
	public void emit(CodeVisitor visitor) {
		visitor._ignored(this);
	}

	public static class Add extends AccountingNode {

		public final int cost;

		public Add(int cost) {
			this.cost = cost;
		}

		@Override
		public String toString() {
			return "CPU.Add(" + cost + ")";
		}

		@Override
		public void emit(CodeVisitor visitor) {
			visitor.visitCpuCheck(cost);
		}

	}

	public static class TickBefore extends AccountingNode {

		@Override
		public String toString() {
			return "CPU.TickBefore";
		}

	}

	public static class Flush extends AccountingNode {

		@Override
		public String toString() {
			return "CPU.Flush";
		}

	}

	public static class End extends AccountingNode {

		@Override
		public String toString() {
			return "CPU.Accounting-End";
		}

	}

}

package net.sandius.rembulan.compiler.gen.block;

public abstract class AccountingNode extends Linear {

	@Override
	public void emit(CodeEmitter e) {
		e._ignored(this);
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

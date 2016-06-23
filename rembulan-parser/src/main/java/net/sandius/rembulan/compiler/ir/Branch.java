package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class Branch extends BlockTermNode implements JmpNode {

	private final Condition condition;
	private final Label branch;
	private final Label next;

	public Branch(Condition condition, Label branch, Label next) {
		this.condition = Check.notNull(condition);
		this.branch = Check.notNull(branch);
		this.next = Check.notNull(next);
	}

	public Condition condition() {
		return condition;
	}

	@Override
	public Label jmpDest() {
		return branch;
	}

	public Label next() {
		return next;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

	public static abstract class Condition {

		private Condition() {
			// not to be instantiated by the outside world
		}

		public abstract void accept(IRVisitor visitor);

		public static class Nil extends Condition {

			private final Temp addr;

			public Nil(Temp addr) {
				this.addr = Check.notNull(addr);
			}

			public Temp addr() {
				return addr;
			}

			@Override
			public void accept(IRVisitor visitor) {
				visitor.visit(this);
			}

		}

		public static class Bool extends Condition {

			private final Temp addr;
			private final boolean expected;

			public Bool(Temp addr, boolean expected) {
				this.addr = Check.notNull(addr);
				this.expected = expected;
			}

			public Temp addr() {
				return addr;
			}

			public boolean expected() {
				return expected;
			}

			@Override
			public void accept(IRVisitor visitor) {
				visitor.visit(this);
			}

		}

		public static class NumLoopEnd extends Condition {

			private final Temp var;
			private final Temp limit;
			private final Temp step;

			public NumLoopEnd(Temp var, Temp limit, Temp step) {
				this.var = Check.notNull(var);
				this.limit = Check.notNull(limit);
				this.step = Check.notNull(step);
			}

			public Temp var() {
				return var;
			}

			public Temp limit() {
				return limit;
			}

			public Temp step() {
				return step;
			}

			@Override
			public void accept(IRVisitor visitor) {
				visitor.visit(this);
			}

		}

	}

}

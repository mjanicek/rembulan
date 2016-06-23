package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	public Iterable<Label> nextLabels() {
		List<Label> tmp = new ArrayList<>(2);
		tmp.add(next());
		tmp.add(jmpDest());
		return Collections.unmodifiableList(tmp);
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

			private final Val addr;

			public Nil(Val addr) {
				this.addr = Check.notNull(addr);
			}

			public Val addr() {
				return addr;
			}

			@Override
			public void accept(IRVisitor visitor) {
				visitor.visit(this);
			}

		}

		public static class Bool extends Condition {

			private final Val addr;
			private final boolean expected;

			public Bool(Val addr, boolean expected) {
				this.addr = Check.notNull(addr);
				this.expected = expected;
			}

			public Val addr() {
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

			private final Val var;
			private final Val limit;
			private final Val step;

			public NumLoopEnd(Val var, Val limit, Val step) {
				this.var = Check.notNull(var);
				this.limit = Check.notNull(limit);
				this.step = Check.notNull(step);
			}

			public Val var() {
				return var;
			}

			public Val limit() {
				return limit;
			}

			public Val step() {
				return step;
			}

			@Override
			public void accept(IRVisitor visitor) {
				visitor.visit(this);
			}

		}

	}

}

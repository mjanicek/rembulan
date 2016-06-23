package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class CheckForEnd extends BranchNode {

	private final Temp var;
	private final Temp limit;
	private final Temp step;

	public CheckForEnd(Temp var, Temp limit, Temp step, Label label, Label next) {
		super(label, next);
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

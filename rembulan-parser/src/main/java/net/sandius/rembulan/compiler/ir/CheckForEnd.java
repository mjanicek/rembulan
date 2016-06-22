package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class CheckForEnd extends IRNode implements JmpNode {

	private final Temp var;
	private final Temp limit;
	private final Temp step;
	private final Label label;

	public CheckForEnd(Temp var, Temp limit, Temp step, Label label) {
		this.var = Check.notNull(var);
		this.limit = Check.notNull(limit);
		this.step = Check.notNull(step);
		this.label = Check.notNull(label);
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
	public Label jmpDest() {
		return label;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

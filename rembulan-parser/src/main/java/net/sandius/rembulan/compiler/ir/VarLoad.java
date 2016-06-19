package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class VarLoad extends IRNode {

	private final Var var;

	public VarLoad(Var var) {
		this.var = Check.notNull(var);
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

	public Var var() {
		return var;
	}

}

package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class VarStore extends IRNode {

	private final Var var;

	public VarStore(Var var) {
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

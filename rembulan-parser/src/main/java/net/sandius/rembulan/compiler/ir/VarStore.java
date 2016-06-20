package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class VarStore extends IRNode {

	private final Var var;
	private final Temp src;

	public VarStore(Var var, Temp src) {
		this.var = Check.notNull(var);
		this.src = Check.notNull(src);
	}


	public Var var() {
		return var;
	}

	public Temp src() {
		return src;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

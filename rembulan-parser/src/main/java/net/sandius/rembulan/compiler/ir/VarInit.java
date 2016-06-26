package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class VarInit extends BodyNode {

	private final Var var;
	private final Val src;

	public VarInit(Var var, Val src) {
		this.var = Check.notNull(var);
		this.src = Check.notNull(src);
	}

	public Var var() {
		return var;
	}

	public Val src() {
		return src;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class VarLoad extends BodyNode {

	private final Val dest;
	private final Var var;

	public VarLoad(Val dest, Var var) {
		this.dest = Check.notNull(dest);
		this.var = Check.notNull(var);
	}

	public Val dest() {
		return dest;
	}

	public Var var() {
		return var;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

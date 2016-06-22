package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class VarLoad extends BodyNode {

	private final Temp dest;
	private final Var var;

	public VarLoad(Temp dest, Var var) {
		this.dest = Check.notNull(dest);
		this.var = Check.notNull(var);
	}

	public Temp dest() {
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

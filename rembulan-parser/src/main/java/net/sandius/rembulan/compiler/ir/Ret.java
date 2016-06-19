package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class Ret extends IRNode {

	private final List<Temp> args;

	public Ret(List<Temp> args) {
		this.args = Check.notNull(args);
	}

	public List<Temp> args() {
		return args;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

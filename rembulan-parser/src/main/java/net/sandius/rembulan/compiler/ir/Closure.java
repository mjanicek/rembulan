package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class Closure extends BodyNode {

	private final Temp dest;
	private final List<Var> args;

	public Closure(Temp dest, List<Var> args) {
		this.dest = Check.notNull(dest);
		this.args = Check.notNull(args);
	}

	public Temp dest() {
		return dest;
	}

	public List<Var> args() {
		return args;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class Closure extends BodyNode {

	private final Val dest;
	private final int idx;
	private final List<Var> args;

	public Closure(Val dest, int idx, List<Var> args) {
		this.dest = Check.notNull(dest);
		this.idx = idx;
		this.args = Check.notNull(args);
	}

	public Val dest() {
		return dest;
	}

	public int idx() {
		return idx;
	}

	public List<Var> args() {
		return args;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

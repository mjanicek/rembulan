package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.compiler.FunctionId;
import net.sandius.rembulan.util.Check;

import java.util.List;

public class Closure extends BodyNode {

	private final Val dest;
	private final FunctionId id;
	private final List<AbstractVar> args;

	public Closure(Val dest, FunctionId id, List<AbstractVar> args) {
		this.dest = Check.notNull(dest);
		this.id = Check.notNull(id);
		this.args = Check.notNull(args);
	}

	public Val dest() {
		return dest;
	}

	public FunctionId id() {
		return id;
	}

	public List<AbstractVar> args() {
		return args;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

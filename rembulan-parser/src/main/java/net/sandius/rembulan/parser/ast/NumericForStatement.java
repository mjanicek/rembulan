package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class NumericForStatement implements Statement {

	private final Name name;
	private final Expr init;
	private final Expr limit;
	private final Expr step;  // may be null
	private final Block block;

	public NumericForStatement(Name name, Expr init, Expr limit, Expr step, Block block) {
		this.name = Check.notNull(name);
		this.init = Check.notNull(init);
		this.limit = Check.notNull(limit);
		this.step = step;
		this.block = Check.notNull(block);
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitNumericFor(name, init, limit, step, block);
	}

}

package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class GenericForStatement implements Statement {

	private final List<Name> names;
	private final List<Expr> exprs;
	private final Block block;

	public GenericForStatement(List<Name> names, List<Expr> exprs, Block block) {
		this.names = Check.notNull(names);
		this.exprs = Check.notNull(exprs);
		this.block = Check.notNull(block);
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitGenericFor(names, exprs, block);
	}

}

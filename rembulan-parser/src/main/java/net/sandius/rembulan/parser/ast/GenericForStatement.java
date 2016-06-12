package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class GenericForStatement extends BodyStatement {

	private final List<Name> names;
	private final List<Expr> exprs;
	private final Block block;

	public GenericForStatement(SourceInfo src, List<Name> names, List<Expr> exprs, Block block) {
		super(src);
		this.names = Check.notNull(names);
		this.exprs = Check.notNull(exprs);
		this.block = Check.notNull(block);
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitGenericFor(names, exprs, block);
	}

}

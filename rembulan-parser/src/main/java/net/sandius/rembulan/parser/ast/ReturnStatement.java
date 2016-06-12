package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class ReturnStatement implements Statement {

	private final SourceInfo src;
	private final List<Expr> exprs;

	public ReturnStatement(SourceInfo src, List<Expr> exprs) {
		this.src = Check.notNull(src);
		this.exprs = Check.notNull(exprs);
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitReturn(exprs);
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

}

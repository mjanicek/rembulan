package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class ReturnStatement extends Statement {

	private final List<Expr> exprs;

	public ReturnStatement(SourceInfo src, List<Expr> exprs) {
		super(src);
		this.exprs = Check.notNull(exprs);
	}

	public List<Expr> exprs() {
		return exprs;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}

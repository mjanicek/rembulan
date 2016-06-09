package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class ReturnStatement {

	private final List<Expr> exprs;

	public ReturnStatement(List<Expr> exprs) {
		this.exprs = Check.notNull(exprs);
	}

}

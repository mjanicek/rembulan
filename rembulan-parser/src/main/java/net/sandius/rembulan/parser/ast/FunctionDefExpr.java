package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class FunctionDefExpr implements Expr {

	private final FunctionLiteral body;

	public FunctionDefExpr(FunctionLiteral body) {
		this.body = Check.notNull(body);
	}

}

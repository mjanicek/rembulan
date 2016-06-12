package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class FunctionDefExpr extends Expr {

	private final FunctionLiteral body;

	public FunctionDefExpr(SourceInfo src, FunctionLiteral body) {
		super(src);
		this.body = Check.notNull(body);
	}

	public FunctionLiteral body() {
		return body;
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visit(this);
	}

}

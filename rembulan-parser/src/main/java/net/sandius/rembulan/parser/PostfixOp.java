package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.Expr;

public abstract class PostfixOp {

	public abstract Expr on(Expr exp);

}

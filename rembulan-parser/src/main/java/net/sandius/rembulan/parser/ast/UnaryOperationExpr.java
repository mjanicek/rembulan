package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class UnaryOperationExpr implements Expr {

	private final Operator.Unary op;
	private final Expr arg;

	public UnaryOperationExpr(Operator.Unary op, Expr arg) {
		this.op = Check.notNull(op);
		this.arg = Check.notNull(arg);
	}

}

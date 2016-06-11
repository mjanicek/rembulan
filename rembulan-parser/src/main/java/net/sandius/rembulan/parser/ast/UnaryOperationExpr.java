package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class UnaryOperationExpr implements RValueExpr {

	private final Operator.Unary op;
	private final Expr arg;

	public UnaryOperationExpr(Operator.Unary op, Expr arg) {
		this.op = Check.notNull(op);
		this.arg = Check.notNull(arg);
	}

	@Override
	public String toString() {
		return "(" + op + " " + arg + ")";
	}

}

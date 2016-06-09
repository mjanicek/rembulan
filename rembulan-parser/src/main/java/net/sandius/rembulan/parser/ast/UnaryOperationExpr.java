package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class UnaryOperationExpr implements Expr {

	private final UnaryOperation op;
	private final Expr arg;

	public UnaryOperationExpr(UnaryOperation op, Expr arg) {
		this.op = Check.notNull(op);
		this.arg = Check.notNull(arg);
	}

}

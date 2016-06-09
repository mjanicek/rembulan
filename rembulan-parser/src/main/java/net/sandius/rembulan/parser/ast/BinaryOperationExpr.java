package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class BinaryOperationExpr implements Expr {

	private final BinaryOperation op;
	private final Expr left;
	private final Expr right;

	public BinaryOperationExpr(BinaryOperation op, Expr left, Expr right) {
		this.op = Check.notNull(op);
		this.left = Check.notNull(left);
		this.right = Check.notNull(right);
	}

}

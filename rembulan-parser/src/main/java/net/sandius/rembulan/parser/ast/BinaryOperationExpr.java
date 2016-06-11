package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class BinaryOperationExpr implements RValueExpr {

	private final Operator.Binary op;
	private final Expr left;
	private final Expr right;

	public BinaryOperationExpr(Operator.Binary op, Expr left, Expr right) {
		this.op = Check.notNull(op);
		this.left = Check.notNull(left);
		this.right = Check.notNull(right);
	}

	@Override
	public String toString() {
		return "(" + op + " " + left + " " + right + ")";
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitBinaryOperation(op, left, right);
	}

}

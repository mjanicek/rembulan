package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class UnaryOperationExpr extends Expr {

	private final Operator.Unary op;
	private final Expr arg;

	public UnaryOperationExpr(SourceInfo src, Operator.Unary op, Expr arg) {
		super(src);
		this.op = Check.notNull(op);
		this.arg = Check.notNull(arg);
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitUnaryOperation(op, arg);
	}

}

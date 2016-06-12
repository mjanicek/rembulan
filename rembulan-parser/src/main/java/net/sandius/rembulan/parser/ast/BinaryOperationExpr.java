package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class BinaryOperationExpr implements RValueExpr {

	private final SourceInfo src;

	private final Operator.Binary op;
	private final Expr left;
	private final Expr right;

	public BinaryOperationExpr(SourceInfo src, Operator.Binary op, Expr left, Expr right) {
		this.src = Check.notNull(src);
		this.op = Check.notNull(op);
		this.left = Check.notNull(left);
		this.right = Check.notNull(right);
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitBinaryOperation(op, left, right);
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

}

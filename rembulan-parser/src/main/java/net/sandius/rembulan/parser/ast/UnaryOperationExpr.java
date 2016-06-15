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

	public Operator.Unary op() {
		return op;
	}

	public Expr arg() {
		return arg;
	}

	public UnaryOperationExpr update(Expr arg) {
		if (this.arg.equals(arg)) {
			return this;
		}
		else {
			return new UnaryOperationExpr(sourceInfo(), op, arg);
		}
	}

	@Override
	public Expr acceptTransformer(Transformer tf) {
		return tf.transform(this);
	}

}

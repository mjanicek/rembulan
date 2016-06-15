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
	public Expr accept(Transformer tf) {
		return tf.transform(this);
	}

}

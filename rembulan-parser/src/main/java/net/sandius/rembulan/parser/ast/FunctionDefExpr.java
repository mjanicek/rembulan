package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class FunctionDefExpr extends Expr {

	private final FunctionLiteral body;

	public FunctionDefExpr(Attributes attr, FunctionLiteral body) {
		super(attr);
		this.body = Check.notNull(body);
	}

	public FunctionLiteral body() {
		return body;
	}

	public FunctionDefExpr update(FunctionLiteral body) {
		if (this.body.equals(body)) {
			return this;
		}
		else {
			return new FunctionDefExpr(attributes(), body);
		}
	}

	@Override
	public Expr accept(Transformer tf) {
		return tf.transform(this);
	}

}

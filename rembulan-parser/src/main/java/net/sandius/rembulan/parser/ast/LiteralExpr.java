package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class LiteralExpr extends Expr {

	private final Literal value;

	public LiteralExpr(Attributes attr, Literal value) {
		super(attr);
		this.value = Check.notNull(value);
	}

	public Literal value() {
		return value;
	}

	public LiteralExpr update(Literal value) {
		if (this.value.equals(value)) {
			return this;
		}
		else {
			return new LiteralExpr(attributes(), value);
		}
	}

	@Override
	public Expr accept(Transformer tf) {
		return tf.transform(this);
	}

}

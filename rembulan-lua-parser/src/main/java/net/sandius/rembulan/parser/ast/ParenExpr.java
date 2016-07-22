package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class ParenExpr extends Expr {

	private final MultiExpr multiExpr;

	public ParenExpr(Attributes attr, MultiExpr multiExpr) {
		super(attr);
		this.multiExpr = Check.notNull(multiExpr);
	}

	public MultiExpr multiExpr() {
		return multiExpr;
	}

	public ParenExpr update(MultiExpr multiExpr) {
		if (this.multiExpr.equals(multiExpr)) {
			return this;
		}
		else {
			return new ParenExpr(attributes(), multiExpr);
		}
	}

	@Override
	public Expr accept(Transformer tf) {
		return tf.transform(this);
	}

}

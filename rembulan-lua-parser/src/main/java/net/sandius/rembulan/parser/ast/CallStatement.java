package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class CallStatement extends BodyStatement {

	private final CallExpr callExpr;

	public CallStatement(Attributes attr, CallExpr callExpr) {
		super(attr);
		this.callExpr = Check.notNull(callExpr);
	}

	public CallExpr callExpr() {
		return callExpr;
	}

	public CallStatement update(CallExpr callExpr) {
		if (this.callExpr.equals(callExpr)) {
			return this;
		}
		else {
			return new CallStatement(attributes(), callExpr);
		}
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}

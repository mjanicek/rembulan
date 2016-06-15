package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class CallStatement extends BodyStatement {

	private final CallExpr callExpr;

	public CallStatement(SourceInfo src, CallExpr callExpr) {
		super(src);
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
			return new CallStatement(sourceInfo(), callExpr);
		}
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}

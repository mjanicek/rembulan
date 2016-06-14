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

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}

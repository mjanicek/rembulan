package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class CallStatement extends BodyStatement {

	private final CallExpr call;

	public CallStatement(CallExpr call) {
		this.call = Check.notNull(call);
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitCall(call);
	}

}

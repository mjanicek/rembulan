package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class CallStatement extends BodyStatement {

	private final CallExpr call;

	public CallStatement(SourceInfo src, CallExpr call) {
		super(src);
		this.call = Check.notNull(call);
	}

	@Override
	public void accept(StatementVisitor visitor) {
		visitor.visitCall(call);
	}

}

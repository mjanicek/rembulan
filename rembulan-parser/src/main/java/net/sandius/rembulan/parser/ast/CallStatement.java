package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

public class CallStatement implements Statement {

	private final CallExpr call;

	public CallStatement(CallExpr call) {
		this.call = Check.notNull(call);
	}

}

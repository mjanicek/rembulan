package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.CallExpr;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.Name;
import net.sandius.rembulan.util.Check;

import java.util.List;

class InvokeOp extends PostfixOp {

	private final List<Expr> args;
	private final Name method;  // may be null

	public InvokeOp(List<Expr> args, Name method) {
		this.args = Check.notNull(args);
		this.method = method;
	}

	@Override
	public CallExpr on(Expr exp) {
		return method != null
				? new CallExpr.MethodCallExpr(exp, method, args)
				: new CallExpr.FunctionCallExpr(exp, args);
	}

}

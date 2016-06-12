package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.CallExpr;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.Name;
import net.sandius.rembulan.util.Check;

import java.util.List;

class InvokeOp extends PostfixOp {

	private final Name method;  // may be null
	private final SourceElement<List<Expr>> args;

	public InvokeOp(SourceElement<List<Expr>> args, Name method) {
		this.args = Check.notNull(args);
		this.method = method;
	}

	@Override
	public CallExpr on(Expr exp) {
		return method != null
				? new CallExpr.MethodCallExpr(args.sourceInfo(), exp, method, args.element())
				: new CallExpr.FunctionCallExpr(args.sourceInfo(), exp, args.element());
	}

}

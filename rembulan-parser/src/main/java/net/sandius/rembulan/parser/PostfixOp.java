package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.CallExpr;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.IndexExpr;
import net.sandius.rembulan.parser.ast.Name;
import net.sandius.rembulan.parser.ast.SourceInfo;
import net.sandius.rembulan.util.Check;

import java.util.List;

abstract class PostfixOp {

	public abstract Expr on(Expr exp);

	static class FieldAccess extends PostfixOp {

		private final SourceInfo src;
		private final Expr keyExpr;

		public FieldAccess(SourceInfo src, Expr keyExpr) {
			this.src = Check.notNull(src);
			this.keyExpr = Check.notNull(keyExpr);
		}

		public Expr keyExpr() {
			return keyExpr;
		}

		@Override
		public IndexExpr on(Expr exp) {
			return new IndexExpr(src, exp, keyExpr);
		}

	}

	static class Invoke extends PostfixOp {

		private final Name method;  // may be null
		private final SourceElement<List<Expr>> args;

		public Invoke(SourceElement<List<Expr>> args, Name method) {
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

}

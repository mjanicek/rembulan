package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public abstract class CallExpr implements RValueExpr {

	private final SourceInfo src;
	private final List<Expr> args;

	public CallExpr(SourceInfo src, List<Expr> args) {
		this.src = Check.notNull(src);
		this.args = Check.notNull(args);
	}

	public List<Expr> args() {
		return args;
	}

	@Override
	public SourceInfo sourceInfo() {
		return src;
	}

	public static class FunctionCallExpr extends CallExpr {

		private final Expr fn;

		public FunctionCallExpr(SourceInfo src, Expr fn, List<Expr> args) {
			super(src, args);
			this.fn = Check.notNull(fn);
		}

		@Override
		public void accept(ExprVisitor visitor) {
			visitor.visitFunctionCall(fn, args());
		}

	}

	public static class MethodCallExpr extends CallExpr {

		private final Expr target;
		private final Name methodName;

		public MethodCallExpr(SourceInfo src, Expr target, Name methodName, List<Expr> args) {
			super(src, args);
			this.target = Check.notNull(target);
			this.methodName = Check.notNull(methodName);
		}

		@Override
		public void accept(ExprVisitor visitor) {
			visitor.visitMethodCall(target, methodName, args());
		}

	}

}

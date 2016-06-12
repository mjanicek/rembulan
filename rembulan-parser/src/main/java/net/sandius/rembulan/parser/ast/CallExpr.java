package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public abstract class CallExpr extends Expr {

	private final List<Expr> args;

	public CallExpr(SourceInfo src, List<Expr> args) {
		super(src);
		this.args = Check.notNull(args);
	}

	public List<Expr> args() {
		return args;
	}

	public static class FunctionCallExpr extends CallExpr {

		private final Expr fn;

		public FunctionCallExpr(SourceInfo src, Expr fn, List<Expr> args) {
			super(src, args);
			this.fn = Check.notNull(fn);
		}

		public Expr fn() {
			return fn;
		}

		@Override
		public void accept(ExprVisitor visitor) {
			visitor.visit(this);
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

		public Expr target() {
			return target;
		}

		public Name methodName() {
			return methodName;
		}

		@Override
		public void accept(ExprVisitor visitor) {
			visitor.visit(this);
		}

	}

}

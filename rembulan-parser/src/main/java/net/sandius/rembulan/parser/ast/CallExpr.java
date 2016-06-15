package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public abstract class CallExpr extends Expr {

	protected final List<Expr> args;

	protected CallExpr(SourceInfo src, Attributes attr, List<Expr> args) {
		super(src, attr);
		this.args = Check.notNull(args);
	}

	public List<Expr> args() {
		return args;
	}

	public static class FunctionCallExpr extends CallExpr {

		private final Expr fn;

		public FunctionCallExpr(SourceInfo src, Attributes attr, Expr fn, List<Expr> args) {
			super(src, attr, args);
			this.fn = Check.notNull(fn);
		}

		public FunctionCallExpr(SourceInfo src, Expr fn, List<Expr> args) {
			this(src, Attributes.empty(), fn, args);
		}

		public Expr fn() {
			return fn;
		}

		public FunctionCallExpr update(Expr fn, List<Expr> args) {
			if (this.fn.equals(fn) && this.args.equals(args)) {
				return this;
			}
			else {
				return new FunctionCallExpr(sourceInfo(), attributes(), fn, args);
			}
		}

		@Override
		public Expr accept(Transformer tf) {
			return tf.transform(this);
		}

	}

	public static class MethodCallExpr extends CallExpr {

		private final Expr target;
		private final Name methodName;

		public MethodCallExpr(SourceInfo src, Attributes attr, Expr target, Name methodName, List<Expr> args) {
			super(src, attr, args);
			this.target = Check.notNull(target);
			this.methodName = Check.notNull(methodName);
		}

		public MethodCallExpr(SourceInfo src, Expr target, Name methodName, List<Expr> args) {
			this(src, Attributes.empty(), target, methodName, args);
		}

		public Expr target() {
			return target;
		}

		public Name methodName() {
			return methodName;
		}

		public MethodCallExpr update(Expr target, Name methodName, List<Expr> args) {
			if (this.target.equals(target) && this.methodName.equals(methodName) && this.args.equals(args)) {
				return this;
			}
			else {
				return new MethodCallExpr(sourceInfo(), attributes(), target, methodName, args);
			}
		}

		@Override
		public Expr accept(Transformer tf) {
			return tf.transform(this);
		}

	}

}

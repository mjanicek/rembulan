package net.sandius.rembulan.parser.ast;

public class VarargsExpr extends Expr {

	public VarargsExpr(SourceInfo src) {
		super(src);
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitVarargs();
	}

}

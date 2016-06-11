package net.sandius.rembulan.parser.ast;

public class VarargsExpr implements RValueExpr {

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitVarargs();
	}

}

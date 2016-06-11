package net.sandius.rembulan.parser.ast;

public class VarargsExpr implements RValueExpr {

	@Override
	public String toString() {
		return "(varargs)";
	}

	@Override
	public void accept(ExprVisitor visitor) {
		visitor.visitVarargs();
	}

}

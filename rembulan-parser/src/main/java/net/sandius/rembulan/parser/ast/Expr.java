package net.sandius.rembulan.parser.ast;

public interface Expr {

	void accept(ExprVisitor visitor);

}

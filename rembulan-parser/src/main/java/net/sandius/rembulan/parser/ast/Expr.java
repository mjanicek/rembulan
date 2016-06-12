package net.sandius.rembulan.parser.ast;

public interface Expr extends SyntaxElement {

	void accept(ExprVisitor visitor);

}

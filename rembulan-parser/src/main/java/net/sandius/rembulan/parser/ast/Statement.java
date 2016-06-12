package net.sandius.rembulan.parser.ast;

public interface Statement extends SyntaxElement {

	void accept(StatementVisitor visitor);

}

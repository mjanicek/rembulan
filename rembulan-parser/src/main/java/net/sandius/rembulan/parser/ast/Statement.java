package net.sandius.rembulan.parser.ast;

public interface Statement {

	void accept(StatementVisitor visitor);

}

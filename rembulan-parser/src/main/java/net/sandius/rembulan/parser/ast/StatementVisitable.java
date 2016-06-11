package net.sandius.rembulan.parser.ast;

public interface StatementVisitable {

	void accept(StatementVisitor visitor);

}

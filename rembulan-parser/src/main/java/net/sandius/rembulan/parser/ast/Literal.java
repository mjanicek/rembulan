package net.sandius.rembulan.parser.ast;

public interface Literal {

	void accept(LiteralVisitor visitor);

}

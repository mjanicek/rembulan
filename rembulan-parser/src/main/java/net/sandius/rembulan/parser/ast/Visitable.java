package net.sandius.rembulan.parser.ast;

public interface Visitable {

	void accept(Visitor visitor);

}

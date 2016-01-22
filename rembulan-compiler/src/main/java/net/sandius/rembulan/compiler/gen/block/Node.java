package net.sandius.rembulan.compiler.gen.block;

public interface Node {

	void accept(NodeVisitor visitor);

}

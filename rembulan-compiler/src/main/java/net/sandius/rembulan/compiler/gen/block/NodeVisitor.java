package net.sandius.rembulan.compiler.gen.block;

public interface NodeVisitor {

	boolean visitNode(Node node);

	void visitEdge(Node from, Node to);

}

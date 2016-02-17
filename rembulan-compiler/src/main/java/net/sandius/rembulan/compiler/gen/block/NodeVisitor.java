package net.sandius.rembulan.compiler.gen.block;

public abstract class NodeVisitor {

	public abstract boolean visitNode(Node node);

	public void visitEdge(Node from, Node to) {

	}

}

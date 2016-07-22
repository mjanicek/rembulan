package net.sandius.rembulan.lbc.recompiler.gen.block;

public abstract class NodeVisitor {

	public abstract boolean visitNode(Node node);

	public void visitEdge(Node from, Node to) {

	}

}

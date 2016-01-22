package net.sandius.rembulan.compiler.gen;

public abstract class NNode {

	public NNode() {
	}

	protected abstract String selfToString();

	@Override
	public String toString() {
		return selfToString();
	}

	public abstract Iterable<NNode> in();

	public abstract int inDegree();

	public abstract Iterable<NNode> out();

	public abstract int outDegree();

	public abstract void detachIncoming(NNode n);

	public abstract void attachIncoming(NNode n);

	public abstract void replaceOutgoing(NNode n, NNode replacement);

}

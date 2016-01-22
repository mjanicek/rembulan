package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;

import java.util.HashSet;
import java.util.Set;

public abstract class NNode {

	private final Set<NNode> in;

	public NNode() {
		this.in = new HashSet<>();
	}

	protected abstract String selfToString();

	protected abstract String nextToString();

	@Override
	public String toString() {
		return selfToString() + " -> " + nextToString();
	}

	public Iterable<NNode> in() {
		return in;
	}

	public int inDegree() {
		return in.size();
	}

	public abstract Iterable<NNode> out();

	public abstract int outDegree();

	public Slots registerEffect(Prototype proto, Slots slots) {
		return slots;
	}

	public void detachIncoming(NNode n) {
		in.remove(n);
	}

	public void attachIncoming(NNode n) {
		in.add(n);
	}

	public abstract void replaceOutgoing(NNode n, NNode replacement);

}

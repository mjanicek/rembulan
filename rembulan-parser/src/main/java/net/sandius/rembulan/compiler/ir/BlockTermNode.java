package net.sandius.rembulan.compiler.ir;

public abstract class BlockTermNode extends IRNode {

	public abstract Iterable<Label> nextLabels();

}

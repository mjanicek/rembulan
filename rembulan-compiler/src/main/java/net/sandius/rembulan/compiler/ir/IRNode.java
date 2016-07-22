package net.sandius.rembulan.compiler.ir;

public abstract class IRNode {

	public abstract void accept(IRVisitor visitor);

}

package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TabNew extends IRNode {

	private final Temp dest;
	private final int array;
	private final int hash;

	public TabNew(Temp dest, int array, int hash) {
		this.dest = Check.notNull(dest);
		this.array = array;
		this.hash = hash;
	}

	public Temp dest() {
		return dest;
	}

	public int array() {
		return array;
	}

	public int hash() {
		return hash;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

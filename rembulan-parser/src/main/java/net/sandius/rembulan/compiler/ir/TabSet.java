package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TabSet extends IRNode {

	private final Temp dest;
	private final Temp key;
	private final Temp value;

	public TabSet(Temp dest, Temp key, Temp value) {
		this.dest = Check.notNull(dest);
		this.key = Check.notNull(key);
		this.value = Check.notNull(value);
	}

	public Temp dest() {
		return dest;
	}

	public Temp key() {
		return key;
	}

	public Temp value() {
		return value;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TabGet extends BodyNode {

	private final Temp dest;
	private final Temp obj;
	private final Temp key;

	public TabGet(Temp dest, Temp obj, Temp key) {
		this.dest = Check.notNull(dest);
		this.obj = Check.notNull(obj);
		this.key = Check.notNull(key);
	}

	public Temp dest() {
		return dest;
	}

	public Temp obj() {
		return obj;
	}

	public Temp key() {
		return key;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

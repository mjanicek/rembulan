package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TabGet extends BodyNode {

	private final Val dest;
	private final Val obj;
	private final Val key;

	public TabGet(Val dest, Val obj, Val key) {
		this.dest = Check.notNull(dest);
		this.obj = Check.notNull(obj);
		this.key = Check.notNull(key);
	}

	public Val dest() {
		return dest;
	}

	public Val obj() {
		return obj;
	}

	public Val key() {
		return key;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

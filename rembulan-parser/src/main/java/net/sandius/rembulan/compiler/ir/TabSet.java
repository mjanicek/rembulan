package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TabSet extends BodyNode {

	private final Val dest;
	private final Val key;
	private final Val value;

	public TabSet(Val dest, Val key, Val value) {
		this.dest = Check.notNull(dest);
		this.key = Check.notNull(key);
		this.value = Check.notNull(value);
	}

	public Val dest() {
		return dest;
	}

	public Val key() {
		return key;
	}

	public Val value() {
		return value;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

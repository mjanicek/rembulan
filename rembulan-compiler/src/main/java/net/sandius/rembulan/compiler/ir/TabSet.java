package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class TabSet extends BodyNode {

	private final Val obj;
	private final Val key;
	private final Val value;

	public TabSet(Val obj, Val key, Val value) {
		this.obj = Check.notNull(obj);
		this.key = Check.notNull(key);
		this.value = Check.notNull(value);
	}

	public Val obj() {
		return obj;
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

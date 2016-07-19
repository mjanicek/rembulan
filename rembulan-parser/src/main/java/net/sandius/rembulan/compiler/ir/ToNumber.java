package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.util.Check;

public class ToNumber extends BodyNode {

	private final Val dest;
	private final Val src;

	private final String desc;

	public ToNumber(Val dest, Val src, String desc) {
		this.dest = Check.notNull(dest);
		this.src = Check.notNull(src);
		this.desc = desc;
	}

	public Val dest() {
		return dest;
	}

	public Val src() {
		return src;
	}

	public String desc() {
		return desc;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

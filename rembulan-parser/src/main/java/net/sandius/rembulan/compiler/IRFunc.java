package net.sandius.rembulan.compiler;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class IRFunc {

	private final Blocks blocks;
	private final List<IRFunc> nested;

	public IRFunc(Blocks blocks, List<IRFunc> nested) {
		this.blocks = Check.notNull(blocks);
		this.nested = Check.notNull(nested);
	}

	public Blocks blocks() {
		return blocks;
	}

	public List<IRFunc> nested() {
		return nested;
	}

}

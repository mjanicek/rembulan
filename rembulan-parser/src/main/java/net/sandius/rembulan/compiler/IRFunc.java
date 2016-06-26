package net.sandius.rembulan.compiler;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class IRFunc {

	private final FunctionId id;
	private final Blocks blocks;
	private final List<IRFunc> nested;

	public IRFunc(FunctionId id, Blocks blocks, List<IRFunc> nested) {
		this.id = Check.notNull(id);
		this.blocks = Check.notNull(blocks);
		this.nested = Check.notNull(nested);
	}

	public FunctionId id() {
		return id;
	}

	public Blocks blocks() {
		return blocks;
	}

	public List<IRFunc> nested() {
		return nested;
	}

}

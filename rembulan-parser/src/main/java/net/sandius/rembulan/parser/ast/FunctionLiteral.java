package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

// FIXME: is this a good name? it isn't an instance of Literal...
public class FunctionLiteral {

	private final FunctionParams params;
	private final Block block;

	public FunctionLiteral(FunctionParams params, Block block) {
		this.params = Check.notNull(params);
		this.block = Check.notNull(block);
	}

	public FunctionParams params() {
		return params;
	}

	public Block block() {
		return block;
	}

	@Override
	public String toString() {
		return "(fnliteral " + params + " " + block + ")";
	}

}

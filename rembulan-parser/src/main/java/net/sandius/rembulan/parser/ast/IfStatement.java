package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class IfStatement implements Statement {

	private final ConditionalBlock main;
	private final List<ConditionalBlock> elifs;
	private final Block elseBlock;  // may be null

	public IfStatement(ConditionalBlock main, List<ConditionalBlock> elifs, Block elseBlock) {
		this.main = Check.notNull(main);
		this.elifs = Check.notNull(elifs);
		this.elseBlock = elseBlock;
	}

}

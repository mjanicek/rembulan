package net.sandius.rembulan.parser.ast;

import net.sandius.rembulan.util.Check;

import java.util.List;

public class IfStatement extends BodyStatement {

	private final ConditionalBlock main;
	private final List<ConditionalBlock> elifs;
	private final Block elseBlock;  // may be null

	public IfStatement(SourceInfo src, ConditionalBlock main, List<ConditionalBlock> elifs, Block elseBlock) {
		super(src);
		this.main = Check.notNull(main);
		this.elifs = Check.notNull(elifs);
		this.elseBlock = elseBlock;
	}

	public ConditionalBlock main() {
		return main;
	}

	public List<ConditionalBlock> elifs() {
		return elifs;
	}

	public Block elseBlock() {
		return elseBlock;
	}

	public IfStatement update(ConditionalBlock main, List<ConditionalBlock> elifs, Block elseBlock) {
		if (this.main.equals(main) && this.elifs.equals(elifs) && this.elseBlock.equals(elseBlock)) {
			return this;
		}
		else {
			return new IfStatement(sourceInfo(), main, elifs, elseBlock);
		}
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}

package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.BlockTermNode;
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.util.Check;

import java.util.List;

public class BasicBlock {

	private final Label label;
	private final List<BodyNode> body;
	private final BlockTermNode end;

	public BasicBlock(Label label, List<BodyNode> body, BlockTermNode end) {
		this.label = Check.notNull(label);
		this.body = Check.notNull(body);
		this.end = Check.notNull(end);
	}

	public Label label() {
		return label;
	}

	public List<BodyNode> body() {
		return body;
	}

	public BlockTermNode end() {
		return end;
	}

}

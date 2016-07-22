package net.sandius.rembulan.compiler.tf;

import net.sandius.rembulan.compiler.BasicBlock;
import net.sandius.rembulan.compiler.Code;
import net.sandius.rembulan.compiler.CodeVisitor;
import net.sandius.rembulan.compiler.ir.BlockTermNode;
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.IRVisitor;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CodeTransformerVisitor extends CodeVisitor {

	private final List<BasicBlock> basicBlocks;

	private Label label;
	private List<BodyNode> body;
	private BlockTermNode end;

	public CodeTransformerVisitor(IRVisitor visitor) {
		super(visitor);
		this.basicBlocks = new ArrayList<>();
	}

	public CodeTransformerVisitor() {
		this(null);
	}

	public Code result() {
		return Code.of(basicBlocks);
	}

	@Override
	public void visit(Code code) {
		basicBlocks.clear();
		super.visit(code);
	}

	@Override
	public void visit(BasicBlock block) {
		label = block.label();
		body = new ArrayList<>(block.body());
		end = block.end();

		BasicBlock bb = block;
		try {
			preVisit(block);
			super.visit(block);
			postVisit(block);
			bb = new BasicBlock(label, Collections.unmodifiableList(body), end);
		}
		finally {
			label = null;
			body = null;
			end = null;
		}

		basicBlocks.add(block.equals(bb) ? block : bb);
	}

	protected Label currentLabel() {
		return label;
	}

	protected void setLabel(Label l) {
		Check.notNull(l);
		label = l;
	}

	protected List<BodyNode> currentBody() {
		return body;
	}

	protected BlockTermNode currentEnd() {
		return end;
	}

	protected void setEnd(BlockTermNode node) {
		Check.notNull(node);
		end = node;
	}

	protected void preVisit(BasicBlock block) {

	}

	protected void postVisit(BasicBlock block) {

	}

}

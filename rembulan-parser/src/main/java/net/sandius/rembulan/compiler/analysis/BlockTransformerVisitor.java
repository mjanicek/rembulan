package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.BasicBlock;
import net.sandius.rembulan.compiler.Blocks;
import net.sandius.rembulan.compiler.BlocksVisitor;
import net.sandius.rembulan.compiler.ir.BlockTermNode;
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.IRVisitor;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockTransformerVisitor extends BlocksVisitor {

	private final List<BasicBlock> basicBlocks;

	private Label label;
	private List<BodyNode> body;
	private BlockTermNode end;

	public BlockTransformerVisitor(IRVisitor visitor) {
		super(visitor);
		this.basicBlocks = new ArrayList<>();
	}

	public BlockTransformerVisitor() {
		this(null);
	}

	public Blocks result() {
		return Blocks.of(basicBlocks);
	}

	@Override
	public void visit(Blocks blocks) {
		basicBlocks.clear();
		super.visit(blocks);
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

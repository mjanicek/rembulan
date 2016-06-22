package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.BlockTermNode;
import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.ir.JmpNode;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.compiler.ir.ToNext;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BlockBuilder {

	private final ArrayList<Block> blocks;
	private final Map<Label, Integer> pending;

	public BlockBuilder(Label entryLabel) {
		this.blocks = new ArrayList<>();
		this.pending = new HashMap<>();

		blocks.add(new Block(entryLabel));
	}

	public Blocks build() {
		ArrayList<BasicBlock> blks = new ArrayList<>();
		for (Block b : blocks) {
			blks.add(b.toBasicBlock());
		}
		return new Blocks(blks);
	}

	private void appendToCurrentBlock(IRNode node) {
		Block currentBlock = blocks.get(blocks.size() - 1);
		currentBlock.insns.add(node);
	}

	private IRNode lastNode() {
		Block currentBlock = blocks.get(blocks.size() - 1);
		return currentBlock.lastNode();
	}

	public void add(IRNode node) {
		Check.notNull(node);

		if (node instanceof JmpNode) {
			Label jmpDest = ((JmpNode) node).jmpDest();
			useLabel(jmpDest);
		}

		if (node instanceof Label) {
			defLabel((Label) node);
		}
		else {
			appendToCurrentBlock(node);
		}
	}

	private void useLabel(Label l) {
		int bIdx = findBlock(l);
		if (bIdx < 0) {
			Integer n = pending.get(l);
			if (n != null) {
				pending.put(l, n + 1);
			}
			else {
				pending.put(l, 1);
			}
		}
		else {
			ensureHead(bIdx, l);
		}
	}

	private void defLabel(Label l) {
		if (!(lastNode() instanceof BlockTermNode)) {
			appendToCurrentBlock(new ToNext(l));
		}

		Integer uses = pending.get(l);
		if (uses != null && uses > 1) {
			blocks.add(new Block(l));
			pending.remove(l);
		}
		else {
			appendToCurrentBlock(l);
		}
	}

	private int findBlock(Label l) {
		for (int i = 0; i < blocks.size(); i++) {
			Block b = blocks.get(i);
			if (b.head.equals(l) || b.insns.contains(l)) {
				return i;
			}
		}
		return -1;
	}

	private void ensureHead(int bIdx, Label l) {
		Block b = blocks.get(bIdx);
		if (!b.head.equals(l)) {
			int idx = b.insns.indexOf(l);

			assert (idx >= 0);

			ArrayList<IRNode> pred = new ArrayList<>(b.insns.subList(0, idx));
			ArrayList<IRNode> succ = new ArrayList<>(b.insns.subList(idx + 1, b.insns.size()));

			IRNode predLast = idx > 0 ? pred.get(idx - 1) : null;
			if (!(predLast instanceof BlockTermNode)) {
				pred.add(new ToNext(l));
			}

			// replace blocks
			blocks.set(bIdx, new Block(b.head, pred));
			blocks.add(bIdx + 1, new Block(l, succ));
		}
	}

	private static class Block {

		private final Label head;
		private final ArrayList<IRNode> insns;

		private Block(Label head, ArrayList<IRNode> insns) {
			this.head = Check.notNull(head);
			this.insns = Check.notNull(insns);
		}

		private Block(Label head) {
			this(head, new ArrayList<IRNode>());
		}

		public IRNode lastNode() {
			int idx = insns.size() - 1;
			return idx >= 0 ? insns.get(idx) : null;
		}

		public BasicBlock toBasicBlock() {
			IRNode last = lastNode();
			if (last instanceof BlockTermNode) {
				return new BasicBlock(
						head,
						Collections.unmodifiableList(insns.subList(0, insns.size() - 1)),
						(BlockTermNode) last);
			}
			else {
				throw new IllegalStateException("expecting terminal node at block end");
			}
		}

	}

}

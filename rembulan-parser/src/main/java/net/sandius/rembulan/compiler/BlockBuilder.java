package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.BlockTermNode;
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.Branch;
import net.sandius.rembulan.compiler.ir.JmpNode;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.compiler.ir.ToNext;
import net.sandius.rembulan.parser.util.Util;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockBuilder {

	private final Map<Label, Integer> uses;
	private final Set<Label> pending;
	private final Set<Label> visited;

	private final List<BasicBlock> basicBlocks;

	private int labelIdx;

	private Block currentBlock;

	public BlockBuilder() {
		this.uses = new HashMap<>();
		this.pending = new HashSet<>();
		this.visited = new HashSet<>();

		this.basicBlocks = new ArrayList<>();

		labelIdx = 0;

		this.currentBlock = null;

		add(newLabel());
	}

	public Label newLabel() {
		return new Label(labelIdx++);
	}

	private void closeCurrentBlock(BlockTermNode end) {
		if (currentBlock == null) {
			throw new IllegalStateException("No current block is open");
		}
		basicBlocks.add(currentBlock.toBasicBlock(end));
		currentBlock = null;
	}

	private void appendToCurrentBlock(BodyNode node) {
		if (currentBlock == null) {
			throw new IllegalStateException("Adding a node outside a block");
		}
		currentBlock.add(node);
	}

	public void add(Label label) {
		Check.notNull(label);
		pending.remove(label);

		if (!visited.add(label)) {
			throw new IllegalStateException("Label already used: " + label);
		}

		if (currentBlock != null) {
			closeCurrentBlock(new ToNext(label));
		}

		assert (currentBlock == null);

		currentBlock = new Block(label);
	}

	public void add(BodyNode node) {
		Check.notNull(node);

		if (node instanceof JmpNode) {
			useLabel(((JmpNode) node).jmpDest());
		}

		appendToCurrentBlock(node);
	}

	public void add(BlockTermNode node) {
		Check.notNull(node);

		if (node instanceof JmpNode) {
			useLabel(((JmpNode) node).jmpDest());
		}

		closeCurrentBlock(node);
	}

	public void addBranch(Branch.Condition cond, Label dest) {
		Label next = newLabel();
		add(new Branch(cond, dest, next));
		add(next);
	}

	private int uses(Label l) {
		Check.notNull(l);
		Integer n = uses.get(l);
		return n != null ? n : 0;
	}

	private void useLabel(Label l) {
		Check.notNull(l);
		uses.put(l, uses(l) + 1);
		if (!visited.contains(l)) {
			pending.add(l);
		}
	}

	private static class Block {

		private final Label label;
		private final List<BodyNode> body;

		private Block(Label label) {
			this.label = label;
			this.body = new ArrayList<>();
		}

		public void add(BodyNode n) {
			body.add(n);
		}

		public BasicBlock toBasicBlock(BlockTermNode end) {
			return new BasicBlock(label, Collections.unmodifiableList(body), end);
		}

	}

	public Blocks build() {
		if (!pending.isEmpty()) {
			throw new IllegalStateException("Label(s) not defined: " + Util.iterableToString(pending, ", "));
		}
		if (currentBlock != null) {
			throw new IllegalStateException("Control reaches end of function");
		}

		return new Blocks(new ArrayList<>(basicBlocks));
	}

}

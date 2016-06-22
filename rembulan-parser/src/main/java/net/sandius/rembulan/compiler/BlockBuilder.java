package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.ir.JmpNode;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class BlockBuilder {

	private final ArrayList<Block> blocks;
	private final Map<Label, Integer> pending;

	public BlockBuilder(Label entryLabel) {
		this.blocks = new ArrayList<>();
		this.pending = new HashMap<>();

		blocks.add(new Block(entryLabel));
	}

	public Iterator<IRNode> nodes() {
		return new Iterator<IRNode>() {

			private int idx = 0;
			private Iterator<IRNode> it = null;

			@Override
			public boolean hasNext() {
				if (it == null) {
					if (idx < blocks.size()) {
						Block b = blocks.get(idx);
						it = b.iterator();
					}
					else {
						return false;
					}
				}

				assert (it != null);

				if (it.hasNext()) {
					return true;
				}
				else {
					it = null;
					idx += 1;
					return hasNext();
				}
			}

			@Override
			public IRNode next() {
				if (it != null) {
					return it.next();
				}
				else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	private void appendToCurrentBlock(IRNode node) {
		Block currentBlock = blocks.get(blocks.size() - 1);
		currentBlock.insns.add(node);
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
		Integer uses = pending.get(l);
		if (uses != null && uses > 1) {
			blocks.add(new Block(l));
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

		public Iterator<IRNode> iterator() {
			return new Iterator<IRNode>() {

				private int idx = -1;

				@Override
				public boolean hasNext() {
					return idx < insns.size();
				}

				@Override
				public IRNode next() {
					if (idx < insns.size()) {
						if (idx < 0) {
							idx += 1;
							return head;
						}
						else {
							return insns.get(idx++);
						}
					}
					else {
						throw new NoSuchElementException();
					}
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

			};
		}

	}

}

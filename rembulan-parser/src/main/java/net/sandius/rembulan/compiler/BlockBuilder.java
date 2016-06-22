package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class BlockBuilder {

	private final ArrayList<Block> blocks;

	public BlockBuilder(Label entryLabel) {
		this.blocks = new ArrayList<>();
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

	public void add(IRNode node) {
		Check.notNull(node);

		Block currentBlock = blocks.get(blocks.size() - 1);
		currentBlock.insns.add(node);
	}

	private static class Block {

		private final Label label;
		private final ArrayList<IRNode> insns;

		private Block(Label label) {
			this.label = Check.notNull(label);
			this.insns = new ArrayList<>();
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
							return label;
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

package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Blocks {

	private final ArrayList<BasicBlock> blocks;

	protected Blocks(ArrayList<BasicBlock> blocks) {
		this.blocks = Check.notNull(blocks);
	}

	public Iterator<IRNode> nodes() {
		return new Iterator<IRNode>() {

			private int idx = 0;
			private Iterator<IRNode> it = null;

			@Override
			public boolean hasNext() {
				if (it == null) {
					if (idx < blocks.size()) {
						BasicBlock b = blocks.get(idx);
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

}

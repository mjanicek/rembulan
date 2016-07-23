package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.compiler.ir.BasicBlock;
import net.sandius.rembulan.compiler.ir.Code;
import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.util.Check;

import java.util.*;

public abstract class CodeUtils {

	private CodeUtils() {
		// not to be instantiated or extended
	}

	public static Iterator<IRNode> nodeIterator(Code code) {
		return new NodeIterator(code.blockIterator());
	}

	private static class NodeIterator implements Iterator<IRNode> {

		private final Iterator<BasicBlock> blockIterator;
		private Iterator<IRNode> blockNodeIterator;

		public NodeIterator(Iterator<BasicBlock> blockIterator) {
			this.blockIterator = Check.notNull(blockIterator);
			this.blockNodeIterator = null;
		}

		@Override
		public boolean hasNext() {
			if (blockNodeIterator != null && blockNodeIterator.hasNext()) {
				return true;
			}
			else {
				if (blockIterator.hasNext()) {
					blockNodeIterator = new BlockNodeIterator(blockIterator.next());
					return this.hasNext();
				}
				else {
					return false;
				}
			}
		}

		@Override
		public IRNode next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			else {
				return blockNodeIterator.next();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private static class BlockNodeIterator implements Iterator<IRNode> {

		private final BasicBlock block;
		private int idx;

		public BlockNodeIterator(BasicBlock block) {
			this.block = Check.notNull(block);
			this.idx = 0;
		}

		@Override
		public boolean hasNext() {
			return idx <= block.body().size();
		}

		@Override
		public IRNode next() {
			int i = idx++;

			if (i == block.body().size()) {
				return block.end();
			}
			else if (i < block.body().size()) {
				return block.body().get(i);
			}
			else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	public static Iterable<Label> labelsBreadthFirst(Code code) {
		Check.notNull(code);

		ArrayList<Label> result = new ArrayList<>();
		Set<Label> visited = new HashSet<>();
		Queue<Label> open = new ArrayDeque<>();

		open.add(code.entryLabel());

		while (!open.isEmpty()) {
			Label l = open.poll();
			BasicBlock bb = code.block(l);
			if (visited.add(l)) {
				result.add(l);
				for (Label nxt : bb.end().nextLabels()) {
					open.add(nxt);
				}
			}
		}

		result.trimToSize();
		return result;
	}

	public static Map<Label, Set<Label>> inLabels(Code code) {
		Check.notNull(code);

		Map<Label, Set<Label>> result = new HashMap<>();

		// initialise
		for (Label l : code.labels()) {
			result.put(l, new HashSet<Label>());
		}

		Set<Label> visited = new HashSet<>();
		Stack<Label> open = new Stack<>();

		open.add(code.entryLabel());

		while (!open.isEmpty()) {
			Label l = open.pop();

			// have we seen this block?
			boolean cont = visited.add(l);

			// add all incoming edges (m -> l)
			for (Label m : code.block(l).end().nextLabels()) {
				result.get(m).add(l);

				// continue to that block?
				if (cont) {
					open.add(m);
				}
			}
		}

		return Collections.unmodifiableMap(result);
	}

}

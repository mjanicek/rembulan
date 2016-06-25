package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.compiler.BasicBlock;
import net.sandius.rembulan.compiler.Blocks;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public abstract class BlocksSimplifier {

	private BlocksSimplifier() {
		// not to be instantiated or extended
	}

	private static Set<Label> reachable(Blocks blocks) {
		Set<Label> visited = new HashSet<>();
		Stack<Label> open = new Stack<>();
		open.add(blocks.entryLabel());

		while (!open.isEmpty()) {
			Label l = open.pop();
			if (!visited.contains(l)) {
				visited.add(l);
				BasicBlock b = blocks.getBlock(l);
				for (Label n : b.end().nextLabels()) {
					open.add(n);
				}
			}
		}

		return visited;
	}

	public static Blocks filterUnreachableBlocks(Blocks blocks) {
		Check.notNull(blocks);

		Set<Label> reachable = reachable(blocks);

		List<BasicBlock> result = new ArrayList<>();
		Iterator<BasicBlock> it = blocks.blockIterator();
		while (it.hasNext()) {
			BasicBlock b = it.next();
			if (reachable.contains(b.label())) {
				result.add(b);
			}
		}

		return Blocks.of(result);
	}

}

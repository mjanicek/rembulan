package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.compiler.BasicBlock;
import net.sandius.rembulan.compiler.Blocks;
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.compiler.ir.ToNext;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public abstract class BlocksSimplifier {

	private BlocksSimplifier() {
		// not to be instantiated or extended
	}

	private static boolean visit(Map<Label, Integer> uses, Label l) {
		Integer n = uses.get(l);
		if (n != null) {
			uses.put(l, n + 1);
			return false;
		}
		else {
			uses.put(l, 1);
			return true;
		}
	}

	private static Map<Label, Integer> uses(Blocks blocks) {
		Map<Label, BasicBlock> index = blocks.index();

		Map<Label, Integer> uses = new HashMap<>();
		Stack<Label> open = new Stack<>();
		open.add(blocks.entryLabel());

		while (!open.isEmpty()) {
			Label l = open.pop();
			if (visit(uses, l)) {
				BasicBlock b = index.get(l);
				for (Label n : b.end().nextLabels()) {
					open.add(n);
				}
			}
		}

		return uses;
	}

	public static Blocks filterUnreachableBlocks(Blocks blocks) {
		Check.notNull(blocks);

		Set<Label> reachable = uses(blocks).keySet();

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

	private static BasicBlock merge(BasicBlock a, BasicBlock b) {
		Check.notNull(a);
		Check.notNull(b);

		if (a.end() instanceof ToNext) {
			List<BodyNode> body = new ArrayList<>();
			body.addAll(a.body());
			body.addAll(b.body());
			return new BasicBlock(a.label(), body, b.end());
		}
		else {
			return null;
		}
	}

	private static <T> T nextOrNull(Iterator<T> it) {
		return it.hasNext() ? it.next() : null;
	}

	public static Blocks mergeBlocks(Blocks blocks) {
		Check.notNull(blocks);

		Map<Label, Integer> uses = uses(blocks);
		List<BasicBlock> result = new ArrayList<>();

		Iterator<BasicBlock> it = blocks.blockIterator();

		BasicBlock a = it.next();  // must be non-null
		BasicBlock b = nextOrNull(it);

		while (b != null) {
			if (uses.get(b.label()) < 2) {
				BasicBlock ab = merge(a, b);
				if (ab != null) {
					a = ab;
				}
				else {
					result.add(a);
					a = b;
				}
			}
			else {
				result.add(a);
				a = b;
			}
			b = nextOrNull(it);
		}

		assert (a != null);
		assert (b == null);

		result.add(a);

		return Blocks.of(result);
	}

}

package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.compiler.BasicBlock;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.util.Check;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public abstract class BlockUtils {

	private BlockUtils() {
		// not to be instantiated or extended
	}

	public static Iterable<Label> labelsBreadthFirst(Map<Label, BasicBlock> index, Label entryLabel) {
		Check.notNull(index);
		Check.notNull(entryLabel);

		ArrayList<Label> result = new ArrayList<>();
		Set<Label> visited = new HashSet<>();
		Queue<Label> open = new ArrayDeque<>();

		open.add(entryLabel);

		while (!open.isEmpty()) {
			Label l = open.poll();
			BasicBlock bb = index.get(l);
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

	public static Map<Label, Set<Label>> inLabels(Map<Label, BasicBlock> index, Label entryLabel) {
		Check.notNull(index);
		Check.notNull(entryLabel);

		Map<Label, Set<Label>> result = new HashMap<>();

		// initialise
		for (Label l : index.keySet()) {
			result.put(l, new HashSet<Label>());
		}

		Set<Label> visited = new HashSet<>();
		Stack<Label> open = new Stack<>();

		open.add(entryLabel);

		while (!open.isEmpty()) {
			Label l = open.pop();

			// have we seen this block?
			boolean cont = visited.add(l);

			// add all incoming edges (m -> l)
			for (Label m : index.get(l).end().nextLabels()) {
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

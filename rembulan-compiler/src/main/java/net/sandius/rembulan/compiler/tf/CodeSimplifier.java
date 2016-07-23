package net.sandius.rembulan.compiler.tf;

import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.ir.BasicBlock;
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.Code;
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

public abstract class CodeSimplifier {

	private CodeSimplifier() {
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

	private static Map<Label, Integer> uses(Code code) {
		Map<Label, BasicBlock> index = code.index();

		Map<Label, Integer> uses = new HashMap<>();
		Stack<Label> open = new Stack<>();
		open.add(code.entryLabel());

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

	static Code pruneUnreachableCode(Code code) {
		Check.notNull(code);

		Set<Label> reachable = uses(code).keySet();

		List<BasicBlock> result = new ArrayList<>();
		Iterator<BasicBlock> it = code.blockIterator();
		while (it.hasNext()) {
			BasicBlock b = it.next();
			if (reachable.contains(b.label())) {
				result.add(b);
			}
		}

		return Code.of(result);
	}

	public static IRFunc pruneUnreachableCode(IRFunc fn) {
		return fn.update(pruneUnreachableCode(fn.blocks()));
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

	static Code mergeBlocks(Code code) {
		Check.notNull(code);

		Map<Label, Integer> uses = uses(code);
		List<BasicBlock> result = new ArrayList<>();

		Iterator<BasicBlock> it = code.blockIterator();

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

		return Code.of(result);
	}

	public static IRFunc mergeBlocks(IRFunc fn) {
		return fn.update(mergeBlocks(fn.blocks()));
	}

}

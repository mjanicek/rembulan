package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.compiler.FunctionId;
import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.Module;
import net.sandius.rembulan.compiler.analysis.NestedRefVisitor;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public abstract class ModuleFilter {

	private ModuleFilter() {
		// not to be instantiated or extended
	}

	private static Set<FunctionId> reachableFromMain(Module m) {
		Check.notNull(m);

		Set<FunctionId> visited = new HashSet<>();
		Stack<IRFunc> open = new Stack<>();

		open.add(m.main());
		while (!open.isEmpty()) {
			IRFunc fn = open.pop();
			if (!visited.add(fn.id())) {
				NestedRefVisitor visitor = new NestedRefVisitor();
				visitor.visit(fn);
				for (FunctionId id : visitor.dependencyInfo().nestedRefs()) {
					open.add(m.get(id));
				}
			}
		}

		return Collections.unmodifiableSet(visited);
	}

	public static Module prune(Module m) {
		Check.notNull(m);

		Set<FunctionId> reachable = reachableFromMain(m);

		ArrayList<IRFunc> fns = new ArrayList<>();
		for (IRFunc fn : m.fns()) {
			if (reachable.contains(fn.id())) {
				fns.add(fn);
			}
		}

		if (!fns.equals(m.fns())) {
			return new Module(fns);
		}
		else {
			// no change
			return m;
		}
	}

}

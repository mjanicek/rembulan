/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.compiler.tf;

import net.sandius.rembulan.compiler.FunctionId;
import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.Module;
import net.sandius.rembulan.compiler.analysis.DependencyAnalyser;
import net.sandius.rembulan.compiler.analysis.DependencyInfo;
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
				DependencyInfo depInfo = DependencyAnalyser.analyse(fn);
				for (FunctionId id : depInfo.nestedRefs()) {
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

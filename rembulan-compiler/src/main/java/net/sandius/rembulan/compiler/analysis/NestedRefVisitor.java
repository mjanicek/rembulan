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

package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.FunctionId;
import net.sandius.rembulan.compiler.ir.Closure;
import net.sandius.rembulan.compiler.ir.CodeVisitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class NestedRefVisitor extends CodeVisitor {

	private final Set<FunctionId> ids;

	public NestedRefVisitor() {
		this.ids = new HashSet<>();
	}

	public DependencyInfo dependencyInfo() {
		return new DependencyInfo(Collections.unmodifiableSet(new HashSet<>(ids)));
	}

	@Override
	public void visit(Closure node) {
		ids.add(node.id());
	}

}

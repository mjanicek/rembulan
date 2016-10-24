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

package net.sandius.rembulan.compiler;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public class Module {

	private final List<IRFunc> fns;

	public Module(List<IRFunc> fns) {
		this.fns = Objects.requireNonNull(fns);
		verify();
	}

	private void verify() {
		Set<FunctionId> ids = new HashSet<>();
		boolean hasMain = false;
		for (IRFunc fn : fns) {
			if (!ids.add(fn.id())) {
				throw new IllegalStateException("Function " + fn.id() + " defined more than once");
			}
			if (fn.id().isRoot()) {
				hasMain = true;
			}
		}
		if (!hasMain) {
			throw new IllegalStateException("No main function in module");
		}
	}

	public List<IRFunc> fns() {
		return fns;
	}

	public IRFunc get(FunctionId id) {
		Objects.requireNonNull(id);

		for (IRFunc fn : fns) {
			if (fn.id().equals(id)) {
				return fn;
			}
		}

		throw new NoSuchElementException();
	}

	public IRFunc main() {
		return get(FunctionId.root());
	}

}

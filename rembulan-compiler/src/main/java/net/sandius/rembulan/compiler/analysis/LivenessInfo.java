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

import net.sandius.rembulan.compiler.ir.AbstractVal;
import net.sandius.rembulan.compiler.ir.IRNode;
import net.sandius.rembulan.compiler.ir.Var;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public class LivenessInfo {

	private final Map<IRNode, Entry> entries;
	
	public LivenessInfo(Map<IRNode, Entry> entries) {
		this.entries = Objects.requireNonNull(entries);
	}

	public static class Entry {

		private final Set<Var> var_in;
		private final Set<Var> var_out;
		private final Set<AbstractVal> val_in;
		private final Set<AbstractVal> val_out;

		Entry(Set<Var> var_in, Set<Var> var_out, Set<AbstractVal> val_in, Set<AbstractVal> val_out) {
			this.var_in = Objects.requireNonNull(var_in);
			this.var_out = Objects.requireNonNull(var_out);
			this.val_in = Objects.requireNonNull(val_in);
			this.val_out = Objects.requireNonNull(val_out);
		}

		public Entry immutableCopy() {
			return new Entry(
					Collections.unmodifiableSet(new HashSet<>(var_in)),
					Collections.unmodifiableSet(new HashSet<>(var_out)),
					Collections.unmodifiableSet(new HashSet<>(val_in)),
					Collections.unmodifiableSet(new HashSet<>(val_out)));
		}

		public Set<Var> inVar() {
			return var_in;
		}

		public Set<Var> outVar() {
			return var_out;
		}

		public Set<AbstractVal> inVal() {
			return val_in;
		}

		public Set<AbstractVal> outVal() {
			return val_out;
		}

	}

	public Entry entry(IRNode node) {
		Objects.requireNonNull(node);
		Entry e = entries.get(node);
		if (e == null) {
			throw new NoSuchElementException("No liveness information for " + node);
		}
		else {
			return e;
		}
	}

	public Iterable<Var> liveInVars(IRNode node) {
		return entry(node).inVar();
	}

	public Iterable<Var> liveOutVars(IRNode node) {
		return entry(node).outVar();
	}

	public Iterable<AbstractVal> liveInVals(IRNode node) {
		return entry(node).inVal();
	}

	public Iterable<AbstractVal> liveOutVals(IRNode node) {
		return entry(node).outVal();
	}

}

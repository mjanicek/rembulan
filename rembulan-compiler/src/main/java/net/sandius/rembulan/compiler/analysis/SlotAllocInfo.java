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
import net.sandius.rembulan.compiler.ir.Var;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class SlotAllocInfo {

	private final Map<AbstractVal, Integer> valSlots;
	private final Map<Var, Integer> varSlots;
	private final int numSlots;

	public SlotAllocInfo(Map<AbstractVal, Integer> valSlots, Map<Var, Integer> varSlots) {
		this.valSlots = Objects.requireNonNull(valSlots);
		this.varSlots = Objects.requireNonNull(varSlots);

		int n = 0;
		for (Integer i : varSlots.values()) {
			n = Math.max(n, i);
		}
		for (Integer i : valSlots.values()) {
			n = Math.max(n, i);
		}
		this.numSlots = n + 1;
	}

	public int slotOf(AbstractVal v) {
		Integer idx = valSlots.get(Objects.requireNonNull(v));
		if (idx != null) {
			return idx;
		}
		else {
			throw new NoSuchElementException("Undefined slot for value: " + v);
		}
	}

	public int slotOf(Var v) {
		Integer idx = varSlots.get(Objects.requireNonNull(v));
		if (idx != null) {
			return idx;
		}
		else {
			throw new NoSuchElementException("Undefined slot for variable: " + v);
		}
	}

	public int numSlots() {
		return numSlots;
	}

}

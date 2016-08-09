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

package net.sandius.rembulan.compiler.ir;

import net.sandius.rembulan.parser.util.Util;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.UnmodifiableIterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class Code {

	private final List<BasicBlock> blocks;

	private final Map<Label, BasicBlock> index;

	private Code(List<BasicBlock> blocks) {
		verify(blocks);
		this.blocks = Check.notNull(blocks);
		this.index = index(blocks);
	}

	public static Code of(List<BasicBlock> blocks) {
		return new Code(
				new ArrayList<>(Check.notNull(blocks)));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Code that = (Code) o;
		return this.blocks.equals(that.blocks);
	}

	@Override
	public int hashCode() {
		return blocks.hashCode();
	}

	private static List<BasicBlock> verify(List<BasicBlock> blocks) {
		Check.notNull(blocks);
		if (blocks.isEmpty()) {
			throw new IllegalArgumentException("Empty block sequence");
		}
		Set<Label> defs = new HashSet<>();
		Set<Label> pending = new HashSet<>();
		for (BasicBlock b : blocks) {
			Label l = b.label();
			if (!defs.add(l)) {
				throw new IllegalArgumentException("Label " + l + " defined more than once");
			}
			else {
				pending.remove(l);
			}

			for (Label nxt : b.end().nextLabels()) {
				if (!defs.contains(nxt)) {
					pending.add(nxt);
				}
			}
		}

		if (!pending.isEmpty()) {
			throw new IllegalStateException("Label(s) not defined: " + Util.iterableToString(pending, ", "));
		}

		return blocks;
	}

	private static Map<Label, BasicBlock> index(Iterable<BasicBlock> blocks) {
		Map<Label, BasicBlock> result = new HashMap<>();
		for (BasicBlock b : blocks) {
			result.put(b.label(), b);
		}
		return Collections.unmodifiableMap(result);
	}

	public BasicBlock entryBlock() {
		return blocks.get(0);
	}

	public Label entryLabel() {
		return entryBlock().label();
	}

	public BasicBlock block(Label label) {
		Check.notNull(label);
		BasicBlock result = index.get(label);
		if (result != null) {
			return result;
		}
		else {
			throw new NoSuchElementException("Label not found: " + label);
		}
	}

	public Iterable<Label> labels() {
		return index.keySet();
	}

	public Iterator<BasicBlock> blockIterator() {
		return new UnmodifiableIterator<>(blocks.iterator());
	}

}

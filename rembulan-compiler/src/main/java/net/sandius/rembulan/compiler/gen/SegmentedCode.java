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

package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.ir.BasicBlock;
import net.sandius.rembulan.compiler.ir.Code;
import net.sandius.rembulan.compiler.ir.Label;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SegmentedCode {

	private final List<List<BasicBlock>> segments;

	private final Map<Label, LabelEntry> index;

	public static class LabelEntry {

		public final Label label;
		public final int segmentIdx;
		public final int idx;

		public LabelEntry(Label label, int segmentIdx, int idx) {
			this.label = Objects.requireNonNull(label);
			this.segmentIdx = segmentIdx;
			this.idx = idx;
		}

	}

	private SegmentedCode(List<List<BasicBlock>> segments, Map<Label, LabelEntry> index) {
		this.segments = Objects.requireNonNull(segments);
		this.index = Objects.requireNonNull(index);
	}

	public static SegmentedCode singleton(Code code) {
		List<List<BasicBlock>> blocks = new ArrayList<>();

		List<BasicBlock> blks = new ArrayList<>();
		Iterator<BasicBlock> bit = code.blockIterator();
		while (bit.hasNext()) {
			blks.add(bit.next());
		}
		blocks.add(Collections.unmodifiableList(blks));

		return of(blocks);
	}

	public static SegmentedCode of(List<List<BasicBlock>> segments) {
		List<List<BasicBlock>> segs = Collections.unmodifiableList(segments);

		// build index
		Map<Label, LabelEntry> index = new HashMap<>();
		for (int i = 0; i < segs.size(); i++) {
			int j = 0;
			for (BasicBlock blk : segs.get(i)) {
				index.put(blk.label(), new LabelEntry(blk.label(), i, j));
				j++;
			}
		}

		return new SegmentedCode(segs, Collections.unmodifiableMap(index));
	}

	public List<List<BasicBlock>> segments() {
		return segments;
	}

	public LabelEntry labelEntry(Label l) {
		LabelEntry le = index.get(Objects.requireNonNull(l));
		if (le == null) {
			throw new IllegalStateException("Label not found: " + l);
		}
		else {
			return le;
		}
	}

	public boolean isSingleton() {
		return segments.size() == 1;
	}

}

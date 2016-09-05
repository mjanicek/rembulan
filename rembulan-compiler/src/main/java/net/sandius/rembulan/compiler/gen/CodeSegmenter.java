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
import net.sandius.rembulan.compiler.ir.BodyNode;
import net.sandius.rembulan.compiler.ir.Code;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.compiler.ir.ToNext;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class CodeSegmenter {

	private CodeSegmenter() {
		// not to be instantiated
	}

	private static int blockLength(BasicBlock blk) {
		return blk.body().size() + 1;
	}

	private static class BlockSplit {

		final BasicBlock pred;
		final BasicBlock succ;

		private BlockSplit(BasicBlock pred, BasicBlock succ) {
			this.pred = Check.notNull(pred);
			this.succ = Check.notNull(succ);
		}

	}

	private static BlockSplit splitBlockAt(BasicBlock blk, int index, int splitIdx) {
		List<BodyNode> predBody = blk.body().subList(0, index);
		List<BodyNode> succBody = blk.body().subList(index, blk.body().size());

		Label succLabel = new Label(-(splitIdx + 1));

		BasicBlock pred = new BasicBlock(blk.label(), predBody, new ToNext(succLabel));
		BasicBlock succ = new BasicBlock(succLabel, succBody, blk.end());

		return new BlockSplit(pred, succ);
	}

	public static SegmentedCode segment(Code code, int limit) {
		if (limit <= 0) {
			return SegmentedCode.singleton(code);
		}
		else {
			List<List<BasicBlock>> segmentBlocks = new ArrayList<>();

			List<BasicBlock> currentSegment = new ArrayList<>();
			int count = 0;
			int splitIdx = 0;

			Iterator<BasicBlock> bit = code.blockIterator();
			BasicBlock blk = bit.hasNext() ? bit.next() : null;

			while (blk != null) {
				int len = blockLength(blk);

				if (count + len < limit) {
					// block fits in with a margin, append and fetch next one
					currentSegment.add(blk);
					count += len;

					blk = bit.hasNext() ? bit.next() : null;
				}
				else if (count + len == limit) {
					// fits entirely in, but it's the last one
					currentSegment.add(blk);
					segmentBlocks.add(Collections.unmodifiableList(currentSegment));

					// start new segment
					currentSegment = new ArrayList<>();
					count = 0;

					blk = bit.hasNext() ? bit.next() : null;
				}
				else {
					assert (count + len > limit);

					// split blk and try again
					BlockSplit split = splitBlockAt(blk, limit - count, splitIdx++);

					// current segment is done
					currentSegment.add(split.pred);
					segmentBlocks.add(Collections.unmodifiableList(currentSegment));

					// start new segment
					currentSegment = new ArrayList<>();
					count = 0;

					blk = split.succ;
				}
			}

			if (!currentSegment.isEmpty()) {
				segmentBlocks.add(Collections.unmodifiableList(currentSegment));
			}

			return SegmentedCode.of(segmentBlocks);
		}
	}

}

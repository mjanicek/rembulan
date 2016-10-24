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

import net.sandius.rembulan.util.Check;

import java.util.Objects;

public class TabRawAppendMulti extends BodyNode {

	private final Val obj;
	private final long firstIdx;
	private final MultiVal src;

	public TabRawAppendMulti(Val obj, int firstIdx, MultiVal src) {
		this.obj = Objects.requireNonNull(obj);
		this.firstIdx = Check.positive(firstIdx);
		this.src = Objects.requireNonNull(src);
	}

	public Val obj() {
		return obj;
	}

	public long firstIdx() {
		return firstIdx;
	}

	public MultiVal src() {
		return src;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

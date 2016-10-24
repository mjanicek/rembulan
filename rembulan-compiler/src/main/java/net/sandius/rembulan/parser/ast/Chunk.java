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

package net.sandius.rembulan.parser.ast;

import java.util.Objects;

public class Chunk {

	private final Attributes attr;
	private final Block block;

	public Chunk(Attributes attr, Block block) {
		this.attr = Objects.requireNonNull(attr);
		this.block = Objects.requireNonNull(block);
	}

	public Chunk(Block block) {
		this(Attributes.empty(), block);
	}

	public Attributes attributes() {
		return attr;
	}

	public Block block() {
		return block;
	}

	public Chunk update(Block block) {
		if (this.block.equals(block)) {
			return this;
		}
		else {
			return new Chunk(attributes(), block);
		}
	}

	public Chunk with(Object o) {
		Attributes as = attr.with(o);
		if (this.attributes().equals(as)) {
			return this;
		}
		else {
			return new Chunk(as, block);
		}
	}

}

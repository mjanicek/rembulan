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

import java.util.List;
import java.util.Objects;

public class IfStatement extends BodyStatement {

	private final ConditionalBlock main;
	private final List<ConditionalBlock> elifs;
	private final Block elseBlock;  // may be null

	public IfStatement(Attributes attr, ConditionalBlock main, List<ConditionalBlock> elifs, Block elseBlock) {
		super(attr);
		this.main = Objects.requireNonNull(main);
		this.elifs = Objects.requireNonNull(elifs);
		this.elseBlock = elseBlock;
	}

	public ConditionalBlock main() {
		return main;
	}

	public List<ConditionalBlock> elifs() {
		return elifs;
	}

	public Block elseBlock() {
		return elseBlock;
	}

	public IfStatement update(ConditionalBlock main, List<ConditionalBlock> elifs, Block elseBlock) {
		if (this.main.equals(main) && this.elifs.equals(elifs) && Objects.equals(this.elseBlock, elseBlock)) {
			return this;
		}
		else {
			return new IfStatement(attributes(), main, elifs, elseBlock);
		}
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}

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

public class NumericForStatement extends BodyStatement {

	private final Name name;
	private final Expr init;
	private final Expr limit;
	private final Expr step;  // may be null
	private final Block block;

	public NumericForStatement(Attributes attr, Name name, Expr init, Expr limit, Expr step, Block block) {
		super(attr);
		this.name = Objects.requireNonNull(name);
		this.init = Objects.requireNonNull(init);
		this.limit = Objects.requireNonNull(limit);
		this.step = step;
		this.block = Objects.requireNonNull(block);
	}

	public Name name() {
		return name;
	}

	public Expr init() {
		return init;
	}

	public Expr limit() {
		return limit;
	}

	public Expr step() {
		return step;
	}

	public Block block() {
		return block;
	}

	public NumericForStatement update(Name name, Expr init, Expr limit, Expr step, Block block) {
		if (this.name.equals(name) && this.init.equals(init) && this.limit.equals(limit)
				&& Objects.equals(this.step, step) && this.block.equals(block)) {
			return this;
		}
		else {
			return new NumericForStatement(attributes(), name, init, limit, step, block);
		}
	}

	public NumericForStatement withAttributes(Attributes attr) {
		if (attributes().equals(attr)) return this;
		else return new NumericForStatement(attr, name, init, limit, step, block);
	}

	public NumericForStatement with(Object o) {
		return this.withAttributes(attributes().with(o));
	}

	@Override
	public BodyStatement accept(Transformer tf) {
		return tf.transform(this);
	}

}

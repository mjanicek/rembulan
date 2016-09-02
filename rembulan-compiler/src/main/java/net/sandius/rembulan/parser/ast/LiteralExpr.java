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

import net.sandius.rembulan.util.Check;

public class LiteralExpr extends Expr {

	private final Literal value;

	public LiteralExpr(Attributes attr, Literal value) {
		super(attr);
		this.value = Check.notNull(value);
	}

	public Literal value() {
		return value;
	}

	public LiteralExpr update(Literal value) {
		if (this.value.equals(value)) {
			return this;
		}
		else {
			return new LiteralExpr(attributes(), value);
		}
	}

	@Override
	public Expr accept(Transformer tf) {
		return tf.transform(this);
	}

}

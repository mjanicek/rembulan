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

public class UnaryOperationExpr extends Expr {

	private final Operator.Unary op;
	private final Expr arg;

	public UnaryOperationExpr(Attributes attr, Operator.Unary op, Expr arg) {
		super(attr);
		this.op = Objects.requireNonNull(op);
		this.arg = Objects.requireNonNull(arg);
	}

	public Operator.Unary op() {
		return op;
	}

	public Expr arg() {
		return arg;
	}

	public UnaryOperationExpr update(Expr arg) {
		if (this.arg.equals(arg)) {
			return this;
		}
		else {
			return new UnaryOperationExpr(attributes(), op, arg);
		}
	}

	@Override
	public Expr accept(Transformer tf) {
		return tf.transform(this);
	}

}

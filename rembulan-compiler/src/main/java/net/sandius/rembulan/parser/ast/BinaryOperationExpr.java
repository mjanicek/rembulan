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

public class BinaryOperationExpr extends Expr {

	private final Operator.Binary op;
	private final Expr left;
	private final Expr right;

	public BinaryOperationExpr(Attributes attr, Operator.Binary op, Expr left, Expr right) {
		super(attr);
		this.op = Objects.requireNonNull(op);
		this.left = Objects.requireNonNull(left);
		this.right = Objects.requireNonNull(right);
	}

	public Operator.Binary op() {
		return op;
	}

	public Expr left() {
		return left;
	}

	public Expr right() {
		return right;
	}

	public BinaryOperationExpr update(Expr left, Expr right) {
		if (this.left.equals(left) && this.right.equals(right)) {
			return this;
		}
		else {
			return new BinaryOperationExpr(attributes(), op, left, right);
		}
	}

	@Override
	public Expr accept(Transformer tf) {
		return tf.transform(this);
	}

}

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

public class BinOp extends BodyNode {

	public enum Op {
		ADD,
		SUB,
		MUL,
		DIV,
		MOD,
		IDIV,
		POW,
		CONCAT,
		BAND,
		BOR,
		BXOR,
		SHL,
		SHR,
		EQ,
		NEQ,
		LT,
		LE
	}

	private final Op op;
	private final Val dest;
	private final Val left;
	private final Val right;

	public BinOp(Op op, Val dest, Val left, Val right) {
		this.op = Check.notNull(op);
		this.dest = Check.notNull(dest);
		this.left = Check.notNull(left);
		this.right = Check.notNull(right);
	}

	public Op op() {
		return op;
	}

	public Val dest() {
		return dest;
	}

	public Val left() {
		return left;
	}

	public Val right() {
		return right;
	}

	@Override
	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

}

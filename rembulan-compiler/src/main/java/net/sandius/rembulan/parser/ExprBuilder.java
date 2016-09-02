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

package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.Operator;
import net.sandius.rembulan.parser.ast.SourceInfo;
import net.sandius.rembulan.util.Check;

import java.util.ArrayDeque;
import java.util.Deque;

class ExprBuilder {

	private final Deque<Expr> operandStack;
	private final Deque<SourceElement<Operator>> operatorStack;

	ExprBuilder() {
		this.operandStack = new ArrayDeque<>();
		this.operatorStack = new ArrayDeque<>();
	}

	private static boolean isRightAssociative(Operator op) {
		return op instanceof Operator.Binary && !((Operator.Binary) op).isLeftAssociative();
	}

	// true iff a takes precedence over b
	private static boolean hasLesserPrecedence(Operator newOp, Operator top) {
		Check.notNull(newOp);
		Check.notNull(top);

		return !(newOp instanceof Operator.Unary)
				&& (isRightAssociative(newOp)
						? newOp.precedence() < top.precedence()
						: newOp.precedence() <= top.precedence());
	}

	private void makeOp(SourceElement<Operator> srcOp) {
		SourceInfo src = srcOp.sourceInfo();
		Operator op = srcOp.element();

		if (op instanceof Operator.Binary) {
			Expr r = operandStack.pop();
			Expr l = operandStack.pop();
			operandStack.push(Exprs.binaryOperation(src, (Operator.Binary) op, l, r));
		}
		else if (op instanceof Operator.Unary) {
			Expr a = operandStack.pop();
			operandStack.push(Exprs.unaryOperation(src, (Operator.Unary) op, a));
		}
		else {
			throw new IllegalStateException("Illegal operator: " + op);
		}
	}

	public void addOp(SourceInfo src, Operator op) {
		Check.notNull(src);
		Check.notNull(op);

		while (!operatorStack.isEmpty() && hasLesserPrecedence(op, operatorStack.peek().element())) {
			makeOp(operatorStack.pop());
		}
		operatorStack.push(SourceElement.of(src, op));
	}

	public void addExpr(Expr expr) {
		Check.notNull(expr);
		operandStack.push(expr);
	}

	public Expr build() {
		while (!operatorStack.isEmpty()) {
			makeOp(operatorStack.pop());
		}

		Expr result = operandStack.pop();

		assert (operandStack.isEmpty());
		assert (operatorStack.isEmpty());

		return result;
	}

}

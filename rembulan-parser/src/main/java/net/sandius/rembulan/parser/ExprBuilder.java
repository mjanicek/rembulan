package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.BinaryOperationExpr;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.Operator;
import net.sandius.rembulan.parser.ast.UnaryOperationExpr;
import net.sandius.rembulan.util.Check;

import java.util.Stack;

class ExprBuilder {

	private final Stack<Expr> operandStack;
	private final Stack<Operator> operatorStack;

	ExprBuilder() {
		this.operandStack = new Stack<>();
		this.operatorStack = new Stack<>();
	}

	public void add(Operator.Unary op) {
		add((Operator) op);
	}

	public void add(Operator.Binary op) {
		add((Operator) op);
	}

	private static boolean isRightAssociative(Operator op) {
		return op instanceof Operator.Binary && !((Operator.Binary) op).isLeftAssociative();
	}

	// true iff a takes precedence over b
	private static boolean hasLesserPrecedence(Operator a, Operator top) {
		Check.notNull(a);
		Check.notNull(top);

		return a.precedence() < top.precedence()
				|| (a.precedence() == top.precedence()
				&& isRightAssociative(a));
	}

	private void makeOp(Operator op) {
		if (op instanceof Operator.Binary) {
			Expr r = operandStack.pop();
			Expr l = operandStack.pop();
			operandStack.push(new BinaryOperationExpr((Operator.Binary) op, l, r));
		}
		else if (op instanceof Operator.Unary) {
			Expr a = operandStack.pop();
			operandStack.push(new UnaryOperationExpr((Operator.Unary) op, a));
		}
		else {
			throw new IllegalStateException("Illegal operator: " + op);
		}
	}

	public void add(Operator op) {
		Check.notNull(op);

		while (!operatorStack.isEmpty() && hasLesserPrecedence(op, operatorStack.peek())) {
			makeOp(operatorStack.pop());
		}

		operatorStack.push(op);
	}

	public void add(Expr expr) {
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

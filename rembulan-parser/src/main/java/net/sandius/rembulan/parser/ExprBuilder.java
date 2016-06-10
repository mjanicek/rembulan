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
	private static boolean hasLesserPrecedence(Operator newOp, Operator top) {
		Check.notNull(newOp);
		Check.notNull(top);

		if (newOp instanceof Operator.Unary) {
			return false;
		}

		return (!isRightAssociative(newOp) && newOp.precedence() <= top.precedence())
				|| (isRightAssociative(newOp) && newOp.precedence() < top.precedence());
	}

	private void makeOp(Operator op) {
		if (op instanceof Operator.Binary) {
			Expr r = operandStack.pop();
			Expr l = operandStack.pop();
			Expr e = new BinaryOperationExpr((Operator.Binary) op, l, r);
			System.out.println("pushing expr: " + e);
			operandStack.push(e);
		}
		else if (op instanceof Operator.Unary) {
			Expr a = operandStack.pop();
			Expr e = new UnaryOperationExpr((Operator.Unary) op, a);
			System.out.println("pushing expr: " + e);
			operandStack.push(e);
		}
		else {
			throw new IllegalStateException("Illegal operator: " + op);
		}
	}

	public void add(Operator op) {
		Check.notNull(op);
		System.out.println("adding op: " + op);

		while (!operatorStack.isEmpty() && hasLesserPrecedence(op, operatorStack.peek())) {
			makeOp(operatorStack.pop());
		}

		operatorStack.push(op);
	}

	public void add(Expr expr) {
		Check.notNull(expr);
		System.out.println("adding expr: " + expr);

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

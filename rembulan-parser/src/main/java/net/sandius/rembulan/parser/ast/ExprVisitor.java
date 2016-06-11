package net.sandius.rembulan.parser.ast;

import java.util.List;

public interface ExprVisitor {

	void visitVar(Name name);

	void visitFieldRef(Expr object, Expr key);

	void visitFunctionCall(Expr fn, List<Expr> args);

	void visitMethodCall(Expr target, Name methodName, List<Expr> args);

	void visitFunctionDef(FunctionLiteral fn);

	void visitLiteral(Literal value);

	void visitTableConstructor(List<TableConstructorExpr.FieldInitialiser> fields);

	void visitVarargs();

	void visitBinaryOperation(Operator.Binary op, Expr left, Expr right);

	void visitUnaryOperation(Operator.Unary op, Expr arg);

}

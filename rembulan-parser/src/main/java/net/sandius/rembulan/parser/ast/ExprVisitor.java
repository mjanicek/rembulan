package net.sandius.rembulan.parser.ast;

public interface ExprVisitor {

	void visit(VarExpr node);

	void visit(IndexExpr node);

	void visit(CallExpr.FunctionCallExpr node);

	void visit(CallExpr.MethodCallExpr node);

	void visit(FunctionDefExpr node);

	void visit(LiteralExpr node);

	void visit(TableConstructorExpr node);

	void visit(VarargsExpr node);

	void visit(BinaryOperationExpr node);

	void visit(UnaryOperationExpr node);

}

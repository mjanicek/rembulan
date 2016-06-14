package net.sandius.rembulan.parser.ast;

public interface Visitor {

	void visit(DoStatement node);

	void visit(ReturnStatement node);

	void visit(CallStatement node);

	void visit(AssignStatement node);

	void visit(LocalDeclStatement node);

	void visit(IfStatement node);

	void visit(NumericForStatement node);

	void visit(GenericForStatement node);

	void visit(WhileStatement node);

	void visit(RepeatUntilStatement node);

	void visit(BreakStatement node);

	void visit(GotoStatement node);

	void visit(LabelStatement node);


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


	void visit(NilLiteral node);

	void visit(BooleanLiteral node);

	void visit(Numeral.IntegerNumeral node);

	void visit(Numeral.FloatNumeral node);

	void visit(StringLiteral node);

}

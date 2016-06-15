package net.sandius.rembulan.parser.ast;

public abstract class Visitor {

	public abstract void visit(DoStatement node);

	public abstract void visit(ReturnStatement node);

	public abstract void visit(CallStatement node);

	public abstract void visit(AssignStatement node);

	public abstract void visit(LocalDeclStatement node);

	public abstract void visit(IfStatement node);

	public abstract void visit(NumericForStatement node);

	public abstract void visit(GenericForStatement node);

	public abstract void visit(WhileStatement node);

	public abstract void visit(RepeatUntilStatement node);

	public abstract void visit(BreakStatement node);

	public abstract void visit(GotoStatement node);

	public abstract void visit(LabelStatement node);


	public abstract void visit(VarExpr node);

	public abstract void visit(IndexExpr node);

	public abstract void visit(CallExpr.FunctionCallExpr node);

	public abstract void visit(CallExpr.MethodCallExpr node);

	public abstract void visit(FunctionDefExpr node);

	public abstract void visit(LiteralExpr node);

	public abstract void visit(TableConstructorExpr node);

	public abstract void visit(VarargsExpr node);

	public abstract void visit(BinaryOperationExpr node);

	public abstract void visit(UnaryOperationExpr node);


	public abstract void visit(NilLiteral node);

	public abstract void visit(BooleanLiteral node);

	public abstract void visit(Numeral.IntegerNumeral node);

	public abstract void visit(Numeral.FloatNumeral node);

	public abstract void visit(StringLiteral node);

}

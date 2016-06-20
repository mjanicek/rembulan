package net.sandius.rembulan.parser.ast;

public abstract class Visitor extends Transformer {

	public abstract void visit(Block block);

	@Override
	public Block transform(Block block) {
		visit(block);
		return block;
	}

	public abstract void visit(DoStatement node);

	@Override
	public BodyStatement transform(DoStatement node) {
		visit(node);
		return node;
	}

	public abstract void visit(ReturnStatement node);

	@Override
	public ReturnStatement transform(ReturnStatement node) {
		visit(node);
		return node;
	}

	public abstract void visit(CallStatement node);

	@Override
	public BodyStatement transform(CallStatement node) {
		visit(node);
		return node;
	}

	public abstract void visit(AssignStatement node);

	@Override
	public BodyStatement transform(AssignStatement node) {
		visit(node);
		return node;
	}

	public abstract void visit(LocalDeclStatement node);

	@Override
	public BodyStatement transform(LocalDeclStatement node) {
		visit(node);
		return node;
	}

	public abstract void visit(IfStatement node);

	@Override
	public BodyStatement transform(IfStatement node) {
		visit(node);
		return node;
	}

	public abstract void visit(NumericForStatement node);

	@Override
	public BodyStatement transform(NumericForStatement node) {
		visit(node);
		return node;
	}

	public abstract void visit(GenericForStatement node);

	@Override
	public BodyStatement transform(GenericForStatement node) {
		visit(node);
		return node;
	}

	public abstract void visit(WhileStatement node);

	@Override
	public BodyStatement transform(WhileStatement node) {
		visit(node);
		return node;
	}

	public abstract void visit(RepeatUntilStatement node);

	@Override
	public BodyStatement transform(RepeatUntilStatement node) {
		visit(node);
		return node;
	}

	public abstract void visit(BreakStatement node);

	@Override
	public BodyStatement transform(BreakStatement node) {
		visit(node);
		return node;
	}

	public abstract void visit(GotoStatement node);

	@Override
	public BodyStatement transform(GotoStatement node) {
		visit(node);
		return node;
	}

	public abstract void visit(LabelStatement node);

	@Override
	public BodyStatement transform(LabelStatement node) {
		visit(node);
		return node;
	}

	public abstract void visit(VarExpr node);

	@Override
	public LValueExpr transform(VarExpr e) {
		visit(e);
		return e;
	}

	public abstract void visit(IndexExpr node);

	@Override
	public LValueExpr transform(IndexExpr e) {
		visit(e);
		return e;
	}

	public abstract void visit(CallExpr.FunctionCallExpr node);

	@Override
	public Expr transform(CallExpr.FunctionCallExpr e) {
		visit(e);
		return e;
	}

	public abstract void visit(CallExpr.MethodCallExpr node);

	@Override
	public Expr transform(CallExpr.MethodCallExpr e) {
		visit(e);
		return e;
	}

	public abstract void visit(FunctionDefExpr node);

	@Override
	public Expr transform(FunctionDefExpr e) {
		visit(e);
		return e;
	}

	public abstract void visit(LiteralExpr node);

	@Override
	public Expr transform(LiteralExpr e) {
		visit(e);
		return e;
	}

	public abstract void visit(TableConstructorExpr node);

	@Override
	public Expr transform(TableConstructorExpr e) {
		visit(e);
		return e;
	}

	public abstract void visit(VarargsExpr node);

	@Override
	public Expr transform(VarargsExpr e) {
		visit(e);
		return e;
	}

	public abstract void visit(ParenExpr node);

	@Override
	public Expr transform(ParenExpr e) {
		visit(e);
		return e;
	}

	public abstract void visit(BinaryOperationExpr node);

	@Override
	public Expr transform(BinaryOperationExpr e) {
		visit(e);
		return e;
	}

	public abstract void visit(UnaryOperationExpr node);

	@Override
	public Expr transform(UnaryOperationExpr e) {
		visit(e);
		return e;
	}

	public abstract void visit(NilLiteral node);

	@Override
	public Literal transform(NilLiteral l) {
		visit(l);
		return l;
	}

	public abstract void visit(BooleanLiteral node);

	@Override
	public Literal transform(BooleanLiteral l) {
		visit(l);
		return l;
	}

	public abstract void visit(Numeral.IntegerNumeral node);

	@Override
	public Literal transform(Numeral.IntegerNumeral l) {
		visit(l);
		return l;
	}

	public abstract void visit(Numeral.FloatNumeral node);

	@Override
	public Literal transform(Numeral.FloatNumeral l) {
		visit(l);
		return l;
	}

	public abstract void visit(StringLiteral node);

	@Override
	public Literal transform(StringLiteral l) {
		visit(l);
		return l;
	}

}

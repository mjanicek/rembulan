package net.sandius.rembulan.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Transformer {

	public Chunk transform(Chunk chunk) {
		return chunk.update(transform(chunk.block()));
	}

	public Block transform(Block block) {
		List<BodyStatement> stats = new ArrayList<>();
		for (BodyStatement bs : block.statements()) {
			stats.add(bs.accept(this));
		}
		ReturnStatement ret = block.returnStatement() != null
				? block.returnStatement().accept(this)
				: null;

		return block.update(Collections.unmodifiableList(stats), ret);
	}

	public BodyStatement transform(DoStatement node) {
		return node.update(transform(node.block()));
	}

	public ReturnStatement transform(ReturnStatement node) {
		return node.update(transformExprList(node.exprs()));
	}

	public BodyStatement transform(AssignStatement node) {
		return node.update(transformVarList(node.vars()), transformExprList(node.exprs()));
	}

	public BodyStatement transform(LocalDeclStatement node) {
		return node.update(
				transformNameList(node.names()),
				transformExprList(node.initialisers()));
	}

	public BodyStatement transform(CallStatement node) {
		// transformation result must be a CallExpr -- otherwise throws a ClassCastException
		return node.update((CallExpr) node.callExpr().accept(this));
	}

	public ConditionalBlock transform(ConditionalBlock cb) {
		return cb.update(
				cb.condition().accept(this),
				transform(cb.block()));
	}

	protected List<ConditionalBlock> transformConditionalBlockList(List<ConditionalBlock> cbs) {
		List<ConditionalBlock> result = new ArrayList<>();
		for (ConditionalBlock cb : cbs) {
			result.add(transform(cb));
		}
		return Collections.unmodifiableList(result);
	}

	public BodyStatement transform(IfStatement node) {
		return node.update(
				transform(node.main()),
				transformConditionalBlockList(node.elifs()),
				node.elseBlock() != null ? transform(node.elseBlock()) : null);
	}

	public BodyStatement transform(NumericForStatement node) {
		return node.update(
				transform(node.name()),
				node.init().accept(this),
				node.limit().accept(this),
				node.step() != null ? node.step().accept(this) : null,
				transform(node.block()));
	}

	public BodyStatement transform(GenericForStatement node) {
		return node.update(
				transformNameList(node.names()),
				transformExprList(node.exprs()),
				transform(node.block()));
	}

	public BodyStatement transform(RepeatUntilStatement node) {
		return node.update(
				node.condition().accept(this),
				transform(node.block()));
	}

	public BodyStatement transform(WhileStatement node) {
		return node.update(
				node.condition().accept(this),
				transform(node.block()));
	}

	public BodyStatement transform(BreakStatement node) {
		return node;
	}

	public BodyStatement transform(LabelStatement node) {
		return node;
	}

	public BodyStatement transform(GotoStatement node) {
		return node;
	}

	protected List<Expr> transformExprList(List<Expr> exprs) {
		List<Expr> result = new ArrayList<>();
		for (Expr e : exprs) {
			result.add(e.accept(this));
		}
		return Collections.unmodifiableList(result);
	}

	protected List<LValueExpr> transformVarList(List<LValueExpr> lvalues) {
		List<LValueExpr> result = new ArrayList<>();
		for (LValueExpr e : lvalues) {
			result.add(e.accept(this));
		}
		return Collections.unmodifiableList(result);
	}

	protected List<Name> transformNameList(List<Name> names) {
		List<Name> result = new ArrayList<>();
		for (Name n : names) {
			// TODO
			result.add(transform(n));
		}
		return Collections.unmodifiableList(result);
	}

	public Name transform(Name n) {
		return n;
	}

	public Expr transform(CallExpr.FunctionCallExpr e) {
		return e.update(e.fn().accept(this), transformExprList(e.args()));
	}

	public Expr transform(CallExpr.MethodCallExpr e) {
		return e.update(e.target().accept(this), e.methodName(), transformExprList(e.args()));
	}

	public LValueExpr transform(IndexExpr e) {
		return e.update(e.object().accept(this), e.key().accept(this));
	}

	public LValueExpr transform(VarExpr e) {
		return e;
	}

	public Expr transform(BinaryOperationExpr e) {
		return e.update(e.left().accept(this), e.right().accept(this));
	}

	public Expr transform(UnaryOperationExpr e) {
		return e.update(e.arg().accept(this));
	}

	public Expr transform(LiteralExpr e) {
		return e;
	}

	public Expr transform(VarargsExpr e) {
		return e;
	}

	public Expr transform(FunctionDefExpr e) {
		return e.update(transform(e.params()), transform(e.block()));
	}

	public FunctionDefExpr.Params transform(FunctionDefExpr.Params ps) {
		return ps.update(transformNameList(ps.names()), ps.isVararg());
	}

	public Expr transform(TableConstructorExpr e) {
		List<TableConstructorExpr.FieldInitialiser> result = new ArrayList<>();
		for (TableConstructorExpr.FieldInitialiser fi : e.fields()) {
			result.add(fi.update(fi.key() != null ? fi.key().accept(this) : null, fi.value().accept(this)));
		}
		return e.update(Collections.unmodifiableList(result));
	}

	public Literal transform(NilLiteral l) {
		return l;
	}

	public Literal transform(BooleanLiteral l) {
		return l;
	}

	public Literal transform(Numeral.IntegerNumeral l) {
		return l;
	}

	public Literal transform(Numeral.FloatNumeral l) {
		return l;
	}

	public Literal transform(StringLiteral l) {
		return l;
	}

}

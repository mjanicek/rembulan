package net.sandius.rembulan.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Transformer {

	public Block transform(Block block) {
		List<BodyStatement> stats = new ArrayList<>();
		for (BodyStatement bs : block.statements()) {
			stats.add(bs.acceptTransformer(this));
		}
		ReturnStatement ret = block.returnStatement() != null
				? block.returnStatement().acceptTransformer(this)
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
		return node.update((CallExpr) node.callExpr().acceptTransformer(this));
	}

	public ConditionalBlock transform(ConditionalBlock cb) {
		return cb.update(
				cb.condition().acceptTransformer(this),
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
				transform(node.elseBlock()));
	}

	public BodyStatement transform(NumericForStatement node) {
		return node.update(
				transform(node.name()),
				node.init().acceptTransformer(this),
				node.limit().acceptTransformer(this),
				node.step() != null ? node.step().acceptTransformer(this) : null,
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
				node.condition().acceptTransformer(this),
				transform(node.block()));
	}

	public BodyStatement transform(WhileStatement node) {
		return node.update(
				node.condition().acceptTransformer(this),
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
			result.add(e.acceptTransformer(this));
		}
		return Collections.unmodifiableList(result);
	}

	protected List<LValueExpr> transformVarList(List<LValueExpr> lvalues) {
		List<LValueExpr> result = new ArrayList<>();
		for (LValueExpr e : lvalues) {
			result.add(e.acceptTransformer(this));
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
		return e.update(e.fn().acceptTransformer(this), transformExprList(e.args()));
	}

	public Expr transform(CallExpr.MethodCallExpr e) {
		return e.update(e.target().acceptTransformer(this), e.methodName(), transformExprList(e.args()));
	}

	public LValueExpr transform(IndexExpr e) {
		return e.update(e.object().acceptTransformer(this), e.key().acceptTransformer(this));
	}

	public LValueExpr transform(VarExpr e) {
		return e;
	}

	public Expr transform(BinaryOperationExpr e) {
		return e.update(e.left().acceptTransformer(this), e.right().acceptTransformer(this));
	}

	public Expr transform(UnaryOperationExpr e) {
		return e.update(e.arg().acceptTransformer(this));
	}

	public Expr transform(LiteralExpr e) {
		return e;
	}

	public Expr transform(VarargsExpr e) {
		return e;
	}

	public Expr transform(FunctionDefExpr e) {
		return e;
	}

	public Expr transform(TableConstructorExpr e) {
		List<TableConstructorExpr.FieldInitialiser> result = new ArrayList<>();
		for (TableConstructorExpr.FieldInitialiser fi : e.fields()) {
			result.add(fi.update(fi.key().acceptTransformer(this), fi.value().acceptTransformer(this)));
		}
		return e.update(Collections.unmodifiableList(result));
	}

}

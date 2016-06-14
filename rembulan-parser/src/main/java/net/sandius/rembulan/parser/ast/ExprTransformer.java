package net.sandius.rembulan.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExprTransformer {

	private List<Expr> transform(List<Expr> exprs) {
		List<Expr> result = new ArrayList<>();
		for (Expr e : exprs) {
			result.add(e.acceptTransformer(this));
		}
		return Collections.unmodifiableList(result);
	}

	public Expr transform(CallExpr.FunctionCallExpr e) {
		return e.update(e.fn().acceptTransformer(this), transform(e.args()));
	}

	public Expr transform(CallExpr.MethodCallExpr e) {
		return e.update(e.target().acceptTransformer(this), e.methodName(), transform(e.args()));
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

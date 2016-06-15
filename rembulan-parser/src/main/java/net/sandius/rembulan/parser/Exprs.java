package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.*;

import java.util.List;

public abstract class Exprs {

	private Exprs() {
		// not to be instantiated
	}

	private static Attributes attr() {
		return Attributes.empty();
	}

	public static LiteralExpr literal(SourceInfo src, Literal value) {
		return new LiteralExpr(src, attr(), value);
	}

	public static FunctionDefExpr functionDef(SourceInfo src, FunctionLiteral body) {
		return new FunctionDefExpr(src, attr(), body);
	}

	public static TableConstructorExpr tableConstructor(SourceInfo src, List<TableConstructorExpr.FieldInitialiser> fields) {
		return new TableConstructorExpr(src, attr(), fields);
	}

	public static TableConstructorExpr.FieldInitialiser fieldInitialiser(Expr keyExpr, Expr valueExpr) {
		return new TableConstructorExpr.FieldInitialiser(keyExpr, valueExpr);
	}

	public static IndexExpr index(SourceInfo src, Expr object, Expr key) {
		return new IndexExpr(src, attr(), object, key);
	}

	public static VarExpr var(SourceInfo src, Name name) {
		return new VarExpr(src, attr(), name);
	}

	public static VarargsExpr varargs(SourceInfo src) {
		return new VarargsExpr(src, attr());
	}

	public static CallExpr.FunctionCallExpr functionCall(SourceInfo src, Expr fn, List<Expr> args) {
		return new CallExpr.FunctionCallExpr(src, attr(), fn, args);
	}

	public static CallExpr.MethodCallExpr methodCall(SourceInfo src, Expr target, Name methodName, List<Expr> args) {
		return new CallExpr.MethodCallExpr(src, attr(), target, methodName, args);
	}

	public static BinaryOperationExpr binaryOperation(SourceInfo src, Operator.Binary op, Expr left, Expr right) {
		return new BinaryOperationExpr(src, attr(), op, left,right);
	}

	public static UnaryOperationExpr unaryOperation(SourceInfo src, Operator.Unary op, Expr arg) {
		return new UnaryOperationExpr(src, attr(), op, arg);
	}

}

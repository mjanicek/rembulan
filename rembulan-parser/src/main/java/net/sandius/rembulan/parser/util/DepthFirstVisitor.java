package net.sandius.rembulan.parser.util;

import net.sandius.rembulan.parser.ast.AssignStatement;
import net.sandius.rembulan.parser.ast.BinaryOperationExpr;
import net.sandius.rembulan.parser.ast.CallExpr;
import net.sandius.rembulan.parser.ast.CallStatement;
import net.sandius.rembulan.parser.ast.DoStatement;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.GenericForStatement;
import net.sandius.rembulan.parser.ast.IfStatement;
import net.sandius.rembulan.parser.ast.IndexExpr;
import net.sandius.rembulan.parser.ast.LocalDeclStatement;
import net.sandius.rembulan.parser.ast.NumericForStatement;
import net.sandius.rembulan.parser.ast.RepeatUntilStatement;
import net.sandius.rembulan.parser.ast.ReturnStatement;
import net.sandius.rembulan.parser.ast.TableConstructorExpr;
import net.sandius.rembulan.parser.ast.UnaryOperationExpr;
import net.sandius.rembulan.parser.ast.Visitor;
import net.sandius.rembulan.parser.ast.WhileStatement;

public abstract class DepthFirstVisitor extends Visitor {

	@Override
	public void visit(DoStatement node) {
		visitPreDo(node);
		visit(node.block());
		visitPostDo(node);
	}

	public abstract void visitPreDo(DoStatement node);
	public abstract void visitPostDo(DoStatement node);

	@Override
	public void visit(ReturnStatement node) {
		visitPreReturn(node);
		for (Expr e : node.exprs()) {
			e.acceptTransformer(this);
		}
		visitPostReturn(node);
	}

	public abstract void visitPreReturn(ReturnStatement node);
	public abstract void visitPostReturn(ReturnStatement node);

	@Override
	public void visit(CallStatement node) {
		visitPreCallStatement(node);
		node.callExpr().acceptTransformer(this);
		visitPostCallStatement(node);
	}

	public abstract void visitPreCallStatement(CallStatement node);
	public abstract void visitPostCallStatement(CallStatement node);

	@Override
	public void visit(AssignStatement node) {
		visitPreAssignStatement(node);
		for (Expr e : node.exprs()) {
			e.acceptTransformer(this);
		}
		visitPostAssignStatement(node);
	}

	public abstract void visitPreAssignStatement(AssignStatement node);
	public abstract void visitPostAssignStatement(AssignStatement node);

	@Override
	public void visit(LocalDeclStatement node) {
		visitPreLocalDeclStatement(node);
		for (Expr e : node.initialisers()) {
			e.acceptTransformer(this);
		}
		visitPostLocalDeclStatement(node);
	}

	public abstract void visitPreLocalDeclStatement(LocalDeclStatement node);
	public abstract void visitPostLocalDeclStatement(LocalDeclStatement node);

	@Override
	public void visit(IfStatement node) {
		visitPreIfStatement(node);
		// TODO
		visitPostIfStatement(node);
	}

	public abstract void visitPreIfStatement(IfStatement node);
	public abstract void visitPostIfStatement(IfStatement node);

	@Override
	public void visit(NumericForStatement node) {
		visitPreNumericForStatement(node);
		// TODO
		visitPostNumericForStatement(node);
	}

	public abstract void visitPreNumericForStatement(NumericForStatement node);
	public abstract void visitPostNumericForStatement(NumericForStatement node);

	@Override
	public void visit(GenericForStatement node) {
		visitPreGenericForStatement(node);
		// TODO
		visitPostGenericForStatement(node);
	}

	public abstract void visitPreGenericForStatement(GenericForStatement node);
	public abstract void visitPostGenericForStatement(GenericForStatement node);

	@Override
	public void visit(WhileStatement node) {
		visitPreWhile(node);
		node.condition().acceptTransformer(this);
		visit(node.block());
		visitPostWhile(node);
	}

	public abstract void visitPreWhile(WhileStatement node);
	public abstract void visitPostWhile(WhileStatement node);

	@Override
	public void visit(RepeatUntilStatement node) {
		visitPreRepeat(node);
		visit(node.block());
		node.condition().acceptTransformer(this);
		visitPostRepeat(node);
	}

	public abstract void visitPreRepeat(RepeatUntilStatement node);
	public abstract void visitPostRepeat(RepeatUntilStatement node);

	@Override
	public void visit(IndexExpr node) {
		visitPreIndex(node);
		node.key().acceptTransformer(this);
		node.object().acceptTransformer(this);
		visitPostIndex(node);
	}

	public abstract void visitPreIndex(IndexExpr node);
	public abstract void visitPostIndex(IndexExpr node);

	@Override
	public void visit(CallExpr.FunctionCallExpr node) {
		visitPreFunctionCall(node);
		node.fn().acceptTransformer(this);
		for (Expr e : node.args()) {
			e.acceptTransformer(this);
		}
		visitPostFunctionCall(node);
	}

	public abstract void visitPreFunctionCall(CallExpr.FunctionCallExpr node);
	public abstract void visitPostFunctionCall(CallExpr.FunctionCallExpr node);

	@Override
	public void visit(CallExpr.MethodCallExpr node) {
		visitPreMethodCall(node);
		node.target().acceptTransformer(this);
		for (Expr e : node.args()) {
			e.acceptTransformer(this);
		}
		visitPostMethodCall(node);
	}

	public abstract void visitPreMethodCall(CallExpr.MethodCallExpr node);
	public abstract void visitPostMethodCall(CallExpr.MethodCallExpr node);

	@Override
	public void visit(TableConstructorExpr node) {

	}

	@Override
	public void visit(BinaryOperationExpr node) {
		visitPreBinaryOp(node);
		node.left().acceptTransformer(this);
		node.right().acceptTransformer(this);
		visitPostBinaryOp(node);
	}

	public abstract void visitPreBinaryOp(BinaryOperationExpr node);
	public abstract void visitPostBinaryOp(BinaryOperationExpr node);

	@Override
	public void visit(UnaryOperationExpr node) {
		visitPreUnaryOp(node);
		node.arg().acceptTransformer(this);
		visitPostUnaryOp(node);
	}

	public abstract void visitPreUnaryOp(UnaryOperationExpr node);
	public abstract void visitPostUnaryOp(UnaryOperationExpr node);

}

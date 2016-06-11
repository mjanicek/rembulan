package net.sandius.rembulan.parser.ast;

import java.util.List;

public interface StatementVisitor {

	void visitDo(Block block);

	void visitReturn(List<Expr> exprs);

	void visitCall(CallExpr call);

	void visitAssignment(List<LValueExpr> vars, List<Expr> exprs);

	void visitLocalDecl(List<Name> names, List<Expr> initialisers);

	void visitIf(ConditionalBlock main, List<ConditionalBlock> elifs, Block elseBlock);

	void visitNumericFor(Name name, Expr init, Expr limit, Expr step, Block block);

	void visitGenericFor(List<Name> names, List<Expr> exprs, Block block);

	void visitWhile(Expr condition, Block block);

	void visitRepeatUntil(Expr condition, Block block);

	void visitBreak();

	void visitGoto(Name labelName);

	void visitLabel(Name labelName);

}

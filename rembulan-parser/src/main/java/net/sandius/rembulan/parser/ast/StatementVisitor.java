package net.sandius.rembulan.parser.ast;

public interface StatementVisitor {

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

}

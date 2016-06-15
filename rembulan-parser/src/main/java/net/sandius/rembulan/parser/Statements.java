package net.sandius.rembulan.parser;

import net.sandius.rembulan.parser.ast.*;
import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.List;

public abstract class Statements {

	private Statements() {
		// not to be instantiated
	}

	private static Attributes attr(SourceInfo src) {
		return Attributes.of(src);
	}

	public static ReturnStatement returnStatement(SourceInfo src, List<Expr> exprs) {
		return new ReturnStatement(attr(src), exprs);
	}

	public static DoStatement doStatement(SourceInfo src, Block block) {
		return new DoStatement(attr(src), block);
	}

	public static AssignStatement assignStatement(SourceInfo src, List<LValueExpr> vars, List<Expr> exprs) {
		return new AssignStatement(attr(src), vars, exprs);
	}

	public static LocalDeclStatement localDeclStatement(SourceInfo src, List<Name> names, List<Expr> initialisers) {
		return new LocalDeclStatement(attr(src), names, initialisers);
	}

	public static LocalDeclStatement localDeclStatement(SourceInfo src, List<Name> names) {
		return localDeclStatement(src, names, Collections.<Expr>emptyList());
	}

	public static LocalDeclStatement localDeclStatement(SourceInfo src, Name n) {
		return localDeclStatement(src, Collections.singletonList(Check.notNull(n)));
	}

	public static CallStatement callStatement(SourceInfo src, CallExpr callExpr) {
		return new CallStatement(attr(src), callExpr);
	}

	public static IfStatement ifStatement(SourceInfo src, ConditionalBlock main, List<ConditionalBlock> elifs, Block elseBlock) {
		return new IfStatement(attr(src), main, elifs, elseBlock);
	}

	public static GenericForStatement genericForStatement(SourceInfo src, List<Name> names, List<Expr> exprs, Block block) {
		return new GenericForStatement(attr(src), names, exprs, block);
	}

	public static NumericForStatement numericForStatement(SourceInfo src, Name name, Expr init, Expr limit, Expr step, Block block) {
		return new NumericForStatement(attr(src), name, init, limit, step, block);
	}

	public static WhileStatement whileStatement(SourceInfo src, Expr condition, Block block) {
		return new WhileStatement(attr(src), condition, block);
	}

	public static RepeatUntilStatement repeatUntilStatement(SourceInfo src, Expr condition, Block block) {
		return new RepeatUntilStatement(attr(src), condition, block);
	}

	public static BreakStatement breakStatement(SourceInfo src) {
		return new BreakStatement(attr(src));
	}

	public static LabelStatement labelStatement(SourceInfo src, Name labelName) {
		return new LabelStatement(attr(src), labelName);
	}

	public static GotoStatement gotoStatement(SourceInfo src, Name labelName) {
		return new GotoStatement(attr(src), labelName);
	}

}

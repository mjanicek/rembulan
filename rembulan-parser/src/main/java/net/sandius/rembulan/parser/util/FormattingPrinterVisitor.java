package net.sandius.rembulan.parser.util;

import net.sandius.rembulan.parser.ast.Block;
import net.sandius.rembulan.parser.ast.CallExpr;
import net.sandius.rembulan.parser.ast.ConditionalBlock;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.LValueExpr;
import net.sandius.rembulan.parser.ast.Name;
import net.sandius.rembulan.parser.ast.StatementVisitor;
import net.sandius.rembulan.util.Check;

import java.io.PrintWriter;
import java.util.List;

public class FormattingPrinterVisitor implements StatementVisitor {

	private final PrintWriter out;
	private final String indentString;
	private final int indent;

	FormattingPrinterVisitor(PrintWriter out, String indentString, int indent) {
		this.out = Check.notNull(out);
		this.indentString = Check.notNull(indentString);
		this.indent = indent;
	}

	public FormattingPrinterVisitor(PrintWriter out) {
		this(out, "\t", 0);
	}

	private void doIndent() {
		for (int i = 0; i < indent; i++) {
			out.print(indentString);
		}
	}

	private FormattingPrinterVisitor subVisitor() {
		return new FormattingPrinterVisitor(out, indentString, indent + 1);
	}

	@Override
	public void visitDo(Block block) {
		doIndent();
		out.println("do");
		block.accept(subVisitor());
		out.println("end");
	}

	@Override
	public void visitReturn(List<Expr> exprs) {
		doIndent();
		out.print("return ");
		out.print(Util.listToString(exprs, ", "));
		out.println();
	}

	@Override
	public void visitCall(CallExpr call) {
		doIndent();
		out.println(call);
	}

	@Override
	public void visitAssignment(List<LValueExpr> vars, List<Expr> exprs) {
		doIndent();
		out.print(Util.listToString(vars, ", "));
		out.print(" = ");
		out.print(Util.listToString(exprs, ", "));
		out.println();
	}

	@Override
	public void visitLocalDecl(List<Name> names, List<Expr> initialisers) {
		doIndent();
		out.print("local ");
		out.print(Util.listToString(names, ", "));
		if (!initialisers.isEmpty()) {
			out.print(" = ");
			out.print(Util.listToString(initialisers, ", "));
		}
		out.println();
	}

	private void printConditionalBlock(ConditionalBlock cbl) {
		out.print(cbl.condition());
		out.println(" then");
		cbl.block().accept(subVisitor());
	}

	@Override
	public void visitIf(ConditionalBlock main, List<ConditionalBlock> elifs, Block elseBlock) {
		doIndent();
		out.print("if ");
		printConditionalBlock(main);
		for (ConditionalBlock cbl : elifs) {
			doIndent();
			out.print("elseif ");
			printConditionalBlock(cbl);
		}
		if (elseBlock != null) {
			doIndent();
			out.print("else");
			elseBlock.accept(subVisitor());
		}
		out.println("end");
	}

	@Override
	public void visitNumericFor(Name name, Expr init, Expr limit, Expr step, Block block) {
		doIndent();
		out.print("for ");
		out.print(name);
		out.print(" = ");
		out.print(init);
		out.print(", ");
		out.print(limit);
		if (step != null) {
			out.print(", ");
			out.print(step);
		}
		out.println(" do");
		block.accept(subVisitor());
		out.println("end");
	}

	@Override
	public void visitGenericFor(List<Name> names, List<Expr> exprs, Block block) {
		doIndent();
		out.print("for ");
		out.print(Util.listToString(names, ", "));
		out.print(" in ");
		out.print(Util.listToString(exprs, ", "));
		out.println(" do");
		block.accept(subVisitor());
		out.println("end");
	}

	@Override
	public void visitWhile(Expr condition, Block block) {
		doIndent();
		out.print("while ");
		out.print(condition);
		out.println(" do");
		block.accept(subVisitor());
		out.println("end");
	}

	@Override
	public void visitRepeatUntil(Expr condition, Block block) {
		doIndent();
		out.println("repeat");
		block.accept(subVisitor());
		out.print("until ");
		out.println(condition);
	}

	@Override
	public void visitBreak() {
		doIndent();
		out.println("break");
	}

	@Override
	public void visitGoto(Name labelName) {
		doIndent();
		out.print("goto ");
		out.println(labelName);
	}

	@Override
	public void visitLabel(Name labelName) {
		doIndent();
		out.print("::");
		out.print(labelName);
		out.println("::");
	}

}

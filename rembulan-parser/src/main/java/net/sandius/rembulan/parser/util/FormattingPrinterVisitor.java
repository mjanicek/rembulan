package net.sandius.rembulan.parser.util;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.parser.ast.Block;
import net.sandius.rembulan.parser.ast.CallExpr;
import net.sandius.rembulan.parser.ast.ConditionalBlock;
import net.sandius.rembulan.parser.ast.Expr;
import net.sandius.rembulan.parser.ast.ExprVisitor;
import net.sandius.rembulan.parser.ast.FieldInitialiser;
import net.sandius.rembulan.parser.ast.FunctionLiteral;
import net.sandius.rembulan.parser.ast.FunctionParams;
import net.sandius.rembulan.parser.ast.LValueExpr;
import net.sandius.rembulan.parser.ast.Literal;
import net.sandius.rembulan.parser.ast.LiteralVisitor;
import net.sandius.rembulan.parser.ast.Name;
import net.sandius.rembulan.parser.ast.Operator;
import net.sandius.rembulan.parser.ast.StatementVisitor;
import net.sandius.rembulan.util.Check;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

public class FormattingPrinterVisitor implements StatementVisitor, ExprVisitor, LiteralVisitor {

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

	private void printName(Name n) {
		out.print(n.value());
	}

	private void printExpr(Expr expr) {
		expr.accept(this);
	}

	private void printVarExpr(Expr expr) {
		if (expr instanceof LValueExpr) {
			printExpr(expr);
		}
		else {
			out.print("(");
			printExpr(expr);
			out.print(")");
		}
	}

	private <T extends Expr> void printExprList(Iterable<T> args) {
		Iterator<T> it = args.iterator();
		while (it.hasNext()) {
			it.next().accept(this);
			if (it.hasNext()) {
				out.print(", ");
			}
		}
	}

	private void printNameList(Iterable<Name> names) {
		Iterator<Name> it = names.iterator();
		while (it.hasNext()) {
			printName(it.next());
			if (it.hasNext()) {
				out.print(", ");
			}
		}
	}

	@Override
	public void visitDo(Block block) {
		doIndent();
		out.println("do");
		block.accept(subVisitor());
		doIndent();
		out.println("end");
	}

	@Override
	public void visitReturn(List<Expr> exprs) {
		doIndent();
		out.print("return ");
		printExprList(exprs);
		out.println();
	}

	@Override
	public void visitCall(CallExpr call) {
		doIndent();
		call.accept(this);
		out.println();
	}

	@Override
	public void visitAssignment(List<LValueExpr> vars, List<Expr> exprs) {
		doIndent();
		printExprList(vars);
		out.print(" = ");
		printExprList(exprs);
		out.println();
	}

	@Override
	public void visitLocalDecl(List<Name> names, List<Expr> initialisers) {
		doIndent();
		out.print("local ");
		printNameList(names);
		if (!initialisers.isEmpty()) {
			out.print(" = ");
			printExprList(initialisers);
		}
		out.println();
	}

	private void printConditionalBlock(ConditionalBlock cbl) {
		printExpr(cbl.condition());
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
		doIndent();
		out.println("end");
	}

	@Override
	public void visitNumericFor(Name name, Expr init, Expr limit, Expr step, Block block) {
		doIndent();
		out.print("for ");
		printName(name);
		out.print(" = ");
		printExpr(init);
		out.print(", ");
		printExpr(limit);
		if (step != null) {
			out.print(", ");
			printExpr(step);
		}
		out.println(" do");
		block.accept(subVisitor());
		doIndent();
		out.println("end");
	}

	@Override
	public void visitGenericFor(List<Name> names, List<Expr> exprs, Block block) {
		doIndent();
		out.print("for ");
		printNameList(names);
		out.print(" in ");
		printExprList(exprs);
		out.println(" do");
		block.accept(subVisitor());
		doIndent();
		out.println("end");
	}

	@Override
	public void visitWhile(Expr condition, Block block) {
		doIndent();
		out.print("while ");
		printExpr(condition);
		out.println(" do");
		block.accept(subVisitor());
		doIndent();
		out.println("end");
	}

	@Override
	public void visitRepeatUntil(Expr condition, Block block) {
		doIndent();
		out.println("repeat");
		block.accept(subVisitor());
		out.print("until ");
		printExpr(condition);
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
		printName(labelName);
		out.println();
	}

	@Override
	public void visitLabel(Name labelName) {
		doIndent();
		out.print("::");
		printName(labelName);
		out.println("::");
	}

	@Override
	public void visitVar(Name name) {
		printName(name);
	}

	@Override
	public void visitFieldRef(Expr object, Expr key) {
		printVarExpr(object);
		out.print("[");
		printExpr(key);
		out.print("]");
	}

	@Override
	public void visitFunctionCall(Expr fn, List<Expr> args) {
		printVarExpr(fn);
		out.print("(");
		printExprList(args);
		out.print(")");
	}

	@Override
	public void visitMethodCall(Expr target, Name methodName, List<Expr> args) {
		printVarExpr(target);
		out.print(":");
		printName(methodName);
		out.print("(");
		printExprList(args);
		out.print(")");
	}

	@Override
	public void visitFunctionDef(FunctionLiteral body) {
		out.print("function ");
		out.print("(");
		FunctionParams params = body.params();
		printNameList(params.names());
		if (params.isVararg()) {
			if (!params.names().isEmpty()) {
				out.print(", ");
			}
			out.print("...");
		}
		out.print(")");
		out.println();
		body.block().accept(subVisitor());
		doIndent();
		out.print("end");
	}

	@Override
	public void visitLiteral(Literal value) {
		value.accept(this);
	}

	@Override
	public void visitTableConstructor(List<FieldInitialiser> fields) {
		out.print("{");
		Iterator<FieldInitialiser> it = fields.iterator();
		while (it.hasNext()) {
			FieldInitialiser fi = it.next();
			Expr k = fi.key();
			if (k != null) {
				out.print("[");
				printExpr(k);
				out.print("] = ");
			}
			printExpr(fi.value());

			if (it.hasNext()) {
				out.print(", ");
			}
		}
		out.print("}");
	}

	@Override
	public void visitVarargs() {
		out.print("...");
	}

	private static String binOp(Operator.Binary op) {
		switch (op) {
			case ADD:  return "+";
			case SUB:  return "-";
			case MUL:  return "*";
			case DIV:  return "/";
			case IDIV: return "//";
			case MOD:  return "%";
			case POW:  return "^";

			case CONCAT: return "..";

			case BAND: return "&";
			case BOR:  return "|";
			case BXOR: return "~";
			case SHL:  return "<<";
			case SHR:  return ">>";

			case EQ:  return "==";
			case NEQ: return "~=";
			case LT:  return "<";
			case LE:  return "<=";
			case GT:  return ">";
			case GE:  return ">=";

			case AND: return "and";
			case OR:  return "or";

			default: throw new IllegalArgumentException("Illegal operator: " + op);
		}
	}

	@Override
	public void visitBinaryOperation(Operator.Binary op, Expr left, Expr right) {
		out.print("(");
		printExpr(left);
		out.print(" ");
		out.print(binOp(op));
		out.print(" ");
		printExpr(right);
		out.print(")");
	}

	private static String unOp(Operator.Unary op) {
		switch (op) {
			case UNM:  return "-";
			case BNOT: return "~";
			case LEN:  return "#";
			case NOT:  return "not ";  // note the space
			default: throw new IllegalArgumentException("Illegal operator: " + op);
		}
	}

	@Override
	public void visitUnaryOperation(Operator.Unary op, Expr arg) {
		out.print("(");
		out.print(unOp(op));
		printExpr(arg);
		out.print(")");
	}

	@Override
	public void visitNil() {
		out.print(LuaFormat.NIL);
	}

	@Override
	public void visitBoolean(boolean value) {
		out.print(LuaFormat.toString(value));
	}

	@Override
	public void visitInteger(long value) {
		out.print(LuaFormat.toString(value));
	}

	@Override
	public void visitFloat(double value) {
		out.print(LuaFormat.toString(value));
	}

	@Override
	public void visitString(String value) {
		out.print(LuaFormat.escape(value));
	}

}

package net.sandius.rembulan.parser.util;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.parser.ast.*;
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
	public void visit(DoStatement node) {
		doIndent();
		out.println("do");
		node.block().accept(subVisitor());
		doIndent();
		out.println("end");
	}

	@Override
	public void visit(ReturnStatement node) {
		doIndent();
		out.print("return ");
		printExprList(node.exprs());
		out.println();
	}

	@Override
	public void visit(CallStatement node) {
		doIndent();
		node.callExpr().accept(this);
		out.println();
	}

	@Override
	public void visit(AssignStatement node) {
		doIndent();
		printExprList(node.vars());
		out.print(" = ");
		printExprList(node.exprs());
		out.println();
	}

	@Override
	public void visit(LocalDeclStatement node) {
		doIndent();
		out.print("local ");
		printNameList(node.names());
		if (!node.initialisers().isEmpty()) {
			out.print(" = ");
			printExprList(node.initialisers());
		}
		out.println();
	}

	private void printConditionalBlock(ConditionalBlock cbl) {
		printExpr(cbl.condition());
		out.println(" then");
		cbl.block().accept(subVisitor());
	}

	@Override
	public void visit(IfStatement node) {
		doIndent();
		out.print("if ");
		printConditionalBlock(node.main());
		for (ConditionalBlock cbl : node.elifs()) {
			doIndent();
			out.print("elseif ");
			printConditionalBlock(cbl);
		}
		if (node.elseBlock() != null) {
			doIndent();
			out.print("else");
			node.elseBlock().accept(subVisitor());
		}
		doIndent();
		out.println("end");
	}

	@Override
	public void visit(NumericForStatement node) {
		doIndent();
		out.print("for ");
		printName(node.name());
		out.print(" = ");
		printExpr(node.init());
		out.print(", ");
		printExpr(node.limit());
		if (node.step() != null) {
			out.print(", ");
			printExpr(node.step());
		}
		out.println(" do");
		node.block().accept(subVisitor());
		doIndent();
		out.println("end");
	}

	@Override
	public void visit(GenericForStatement node) {
		doIndent();
		out.print("for ");
		printNameList(node.names());
		out.print(" in ");
		printExprList(node.exprs());
		out.println(" do");
		node.block().accept(subVisitor());
		doIndent();
		out.println("end");
	}

	@Override
	public void visit(WhileStatement node) {
		doIndent();
		out.print("while ");
		printExpr(node.condition());
		out.println(" do");
		node.block().accept(subVisitor());
		doIndent();
		out.println("end");
	}

	@Override
	public void visit(RepeatUntilStatement node) {
		doIndent();
		out.println("repeat");
		node.block().accept(subVisitor());
		out.print("until ");
		printExpr(node.condition());
	}

	@Override
	public void visit(BreakStatement node) {
		doIndent();
		out.println("break");
	}

	@Override
	public void visit(GotoStatement node) {
		doIndent();
		out.print("goto ");
		printName(node.labelName());
		out.println();
	}

	@Override
	public void visit(LabelStatement node) {
		doIndent();
		out.print("::");
		printName(node.labelName());
		out.println("::");
	}

	@Override
	public void visitVar(Name name) {
		printName(name);
	}

	@Override
	public void visitIndex(Expr object, Expr key) {
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
	public void visitFunctionDef(FunctionLiteral fn) {
		out.print("function ");
		out.print("(");
		printNameList(fn.params().names());
		if (fn.params().isVararg()) {
			if (!fn.params().names().isEmpty()) {
				out.print(", ");
			}
			out.print("...");
		}
		out.print(")");
		out.println();
		fn.block().accept(subVisitor());
		doIndent();
		out.print("end");
	}

	@Override
	public void visitLiteral(Literal value) {
		value.accept(this);
	}

	@Override
	public void visitTableConstructor(List<TableConstructorExpr.FieldInitialiser> fields) {
		out.print("{");
		Iterator<TableConstructorExpr.FieldInitialiser> it = fields.iterator();
		while (it.hasNext()) {
			TableConstructorExpr.FieldInitialiser fi = it.next();
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

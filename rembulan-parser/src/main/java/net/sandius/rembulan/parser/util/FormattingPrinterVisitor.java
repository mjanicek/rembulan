package net.sandius.rembulan.parser.util;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.parser.ast.*;
import net.sandius.rembulan.util.Check;

import java.io.PrintWriter;
import java.util.Iterator;

public class FormattingPrinterVisitor extends Visitor {

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
	public void visit(VarExpr node) {
		printName(node.name());
	}

	@Override
	public void visit(IndexExpr node) {
		printVarExpr(node.object());
		out.print("[");
		printExpr(node.key());
		out.print("]");
	}

	@Override
	public void visit(CallExpr.FunctionCallExpr node) {
		printVarExpr(node.fn());
		out.print("(");
		printExprList(node.args());
		out.print(")");
	}

	@Override
	public void visit(CallExpr.MethodCallExpr node) {
		printVarExpr(node.target());
		out.print(":");
		printName(node.methodName());
		out.print("(");
		printExprList(node.args());
		out.print(")");
	}

	@Override
	public void visit(FunctionDefExpr node) {
		out.print("function ");
		out.print("(");

		FunctionLiteral fn = node.body();

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
	public void visit(LiteralExpr node) {
		node.value().accept(this);
	}

	@Override
	public void visit(TableConstructorExpr node) {
		out.print("{");
		Iterator<TableConstructorExpr.FieldInitialiser> it = node.fields().iterator();
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
	public void visit(VarargsExpr node) {
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
	public void visit(BinaryOperationExpr node) {
		out.print("(");
		printExpr(node.left());
		out.print(" ");
		out.print(binOp(node.op()));
		out.print(" ");
		printExpr(node.right());
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
	public void visit(UnaryOperationExpr node) {
		out.print("(");
		out.print(unOp(node.op()));
		printExpr(node.arg());
		out.print(")");
	}

	@Override
	public void visit(NilLiteral node) {
		out.print(LuaFormat.NIL);
	}

	@Override
	public void visit(BooleanLiteral node) {
		out.print(LuaFormat.toString(node.value()));
	}

	@Override
	public void visit(Numeral.IntegerNumeral node) {
		out.print(LuaFormat.toString(node.value()));
	}

	@Override
	public void visit(Numeral.FloatNumeral node) {
		out.print(LuaFormat.toString(node.value()));
	}

	@Override
	public void visit(StringLiteral node) {
		out.print(LuaFormat.escape(node.value()));
	}
}

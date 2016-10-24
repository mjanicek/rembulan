/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.parser.util;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.parser.analysis.FunctionVarInfo;
import net.sandius.rembulan.parser.analysis.ResolvedLabel;
import net.sandius.rembulan.parser.analysis.ResolvedVariable;
import net.sandius.rembulan.parser.analysis.VarMapping;
import net.sandius.rembulan.parser.analysis.Variable;
import net.sandius.rembulan.parser.ast.*;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class FormattingPrinterVisitor extends Visitor {

	private final PrintWriter out;
	private final String indentString;
	private final int indent;

	private final boolean withResolvedNames;

	FormattingPrinterVisitor(PrintWriter out, String indentString, int indent, boolean withResolvedNames) {
		this.out = Objects.requireNonNull(out);
		this.indentString = Objects.requireNonNull(indentString);
		this.indent = indent;
		this.withResolvedNames = withResolvedNames;
	}

	public FormattingPrinterVisitor(PrintWriter out, boolean withResolvedNames) {
		this(out, "\t", 0, withResolvedNames);
	}

	public FormattingPrinterVisitor(PrintWriter out) {
		this(out, false);
	}

	private void doIndent() {
		for (int i = 0; i < indent; i++) {
			out.print(indentString);
		}
	}

	private FormattingPrinterVisitor subVisitor() {
		return new FormattingPrinterVisitor(out, indentString, indent + 1, withResolvedNames);
	}

	private static <T> T getOrNull(List<T> list, int idx) {
		return (list != null && idx >= 0 && idx < list.size()) ? list.get(idx) : null;
	}

	private static Variable mappedVar(VarMapping vm, int idx) {
		return vm != null ? getOrNull(vm.vars(), idx) : null;
	}

	private String varNameToString(Name n, Variable v) {
		if (withResolvedNames) {
			return v == null
					? "_unresolved_" + n.value()
					: v.name().value() + "_" + Integer.toHexString(v.hashCode());
		}
		else {
			return n.value();
		}
	}

	private void printName(Name n, Variable v) {
		out.print(varNameToString(n, v));
	}

	private void printName(Name n, ResolvedVariable rv) {
		final String result;
		if (withResolvedNames) {
			result = rv == null
					? varNameToString(n, null)
					: (rv.isUpvalue() ? "--[[^]]" : "") + varNameToString(n, rv.variable());
		}
		else {
			result = n.value();
		}
		out.print(result);
	}

	private void printLabelName(Name n, ResolvedLabel rl) {
		final String result;
		if (withResolvedNames) {
			result = rl == null
					? "_unresolved_" + n.value()
					: n.value() + "_" + Integer.toHexString(rl.hashCode());
		}
		else {
			result = n.value();
		}
		out.print(result);
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

	private void printNameList(Iterable<Name> names, List<Variable> vars) {
		Iterator<Name> it = names.iterator();
		int i = 0;
		while (it.hasNext()) {
			printName(it.next(), getOrNull(vars, i++));
			if (it.hasNext()) {
				out.print(", ");
			}
		}
	}

	private void printNameList(Iterable<Name> names, VarMapping varMapping) {
		printNameList(names, varMapping != null ? varMapping.vars() : null);
	}

	private void printFixedParamList(Iterable<Name> names, FunctionVarInfo fvi) {
		printNameList(names, fvi != null ? fvi.params() : null);
	}

	@Override
	public void visit(Block block) {
		for (BodyStatement s : block.statements()) {
			s.accept(this);
		}
		if (block.returnStatement() != null) {
			block.returnStatement().accept(this);
		}
	}

	@Override
	public void visit(DoStatement node) {
		doIndent();
		out.println("do");
		visit(node.block());
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
		printNameList(node.names(), node.attributes().get(VarMapping.class));
		if (!node.initialisers().isEmpty()) {
			out.print(" = ");
			printExprList(node.initialisers());
		}
		out.println();
	}

	private void printConditionalBlock(ConditionalBlock cbl) {
		printExpr(cbl.condition());
		out.println(" then");
		subVisitor().visit(cbl.block());
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
			subVisitor().visit(node.elseBlock());
		}
		doIndent();
		out.println("end");
	}

	@Override
	public void visit(NumericForStatement node) {
		doIndent();
		out.print("for ");
		printName(node.name(), mappedVar(node.attributes().get(VarMapping.class), 0));
		out.print(" = ");
		printExpr(node.init());
		out.print(", ");
		printExpr(node.limit());
		if (node.step() != null) {
			out.print(", ");
			printExpr(node.step());
		}
		out.println(" do");
		subVisitor().visit(node.block());
		doIndent();
		out.println("end");
	}

	@Override
	public void visit(GenericForStatement node) {
		doIndent();
		out.print("for ");
		printNameList(node.names(), node.attributes().get(VarMapping.class));
		out.print(" in ");
		printExprList(node.exprs());
		out.println(" do");
		subVisitor().visit(node.block());
		doIndent();
		out.println("end");
	}

	@Override
	public void visit(WhileStatement node) {
		doIndent();
		out.print("while ");
		printExpr(node.condition());
		out.println(" do");
		subVisitor().visit(node.block());
		doIndent();
		out.println("end");
	}

	@Override
	public void visit(RepeatUntilStatement node) {
		doIndent();
		out.println("repeat");
		subVisitor().visit(node.block());
		doIndent();
		out.print("until ");
		printExpr(node.condition());
		out.println();
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
		printLabelName(node.labelName(), node.attributes().get(ResolvedLabel.class));
		out.println();
	}

	@Override
	public void visit(LabelStatement node) {
		doIndent();
		out.print("::");
		printLabelName(node.labelName(), node.attributes().get(ResolvedLabel.class));
		out.println("::");
	}

	@Override
	public void visit(VarExpr node) {
		printName(node.name(), node.attributes().get(ResolvedVariable.class));
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
		out.print(node.methodName().value());
		out.print("(");
		printExprList(node.args());
		out.print(")");
	}

	@Override
	public void visit(FunctionDefExpr node) {
		out.print("function ");
		out.print("(");

		printFixedParamList(node.params().names(), node.attributes().get(FunctionVarInfo.class));
		if (node.params().isVararg()) {
			if (!node.params().names().isEmpty()) {
				out.print(", ");
			}
			out.print("...");
		}
		out.print(")");
		out.println();
		subVisitor().visit(node.block());
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

	@Override
	public void visit(ParenExpr node) {
		out.print("(");
		node.multiExpr().accept(this);
		out.print(")");
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

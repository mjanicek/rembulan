package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.compiler.BasicBlock;
import net.sandius.rembulan.compiler.BlocksVisitor;
import net.sandius.rembulan.compiler.ir.BinOp;
import net.sandius.rembulan.compiler.ir.CJmp;
import net.sandius.rembulan.compiler.ir.Call;
import net.sandius.rembulan.compiler.ir.CheckForEnd;
import net.sandius.rembulan.compiler.ir.Closure;
import net.sandius.rembulan.compiler.ir.Jmp;
import net.sandius.rembulan.compiler.ir.JmpIfNil;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.compiler.ir.LoadConst;
import net.sandius.rembulan.compiler.ir.Mov;
import net.sandius.rembulan.compiler.ir.Ret;
import net.sandius.rembulan.compiler.ir.StackGet;
import net.sandius.rembulan.compiler.ir.TCall;
import net.sandius.rembulan.compiler.ir.TabGet;
import net.sandius.rembulan.compiler.ir.TabNew;
import net.sandius.rembulan.compiler.ir.TabSet;
import net.sandius.rembulan.compiler.ir.TabStackAppend;
import net.sandius.rembulan.compiler.ir.ToNext;
import net.sandius.rembulan.compiler.ir.ToNumber;
import net.sandius.rembulan.compiler.ir.UnOp;
import net.sandius.rembulan.compiler.ir.UpLoad;
import net.sandius.rembulan.compiler.ir.UpStore;
import net.sandius.rembulan.compiler.ir.VList;
import net.sandius.rembulan.compiler.ir.VarLoad;
import net.sandius.rembulan.compiler.ir.VarStore;
import net.sandius.rembulan.compiler.ir.Vararg;
import net.sandius.rembulan.parser.util.Util;
import net.sandius.rembulan.util.Check;

import java.io.PrintWriter;

public class IRPrinterVisitor extends BlocksVisitor {

	private final PrintWriter ps;

	public IRPrinterVisitor(PrintWriter ps) {
		this.ps = Check.notNull(ps);
	}

	@Override
	public void visit(LoadConst.Nil node) {
		ps.println("\tldnil " + node.dest());
	}

	@Override
	public void visit(LoadConst.Bool node) {
		ps.println("\tldbool " + node.dest() + " " + node.value());
	}

	@Override
	public void visit(LoadConst.Int node) {
		ps.println("\tldint " + node.dest() + " " + node.value());
	}

	@Override
	public void visit(LoadConst.Flt node) {
		ps.println("\tldflt " + node.dest() + " " + node.value());
	}

	@Override
	public void visit(LoadConst.Str node) {
		ps.println("\tldstr " + node.dest() + " " + LuaFormat.escape(node.value()));
	}

	@Override
	public void visit(BinOp node) {
		ps.println("\t" + node.op().toString().toLowerCase() + " " + node.dest() + " " + node.left() + " " + node.right());
	}

	@Override
	public void visit(UnOp node) {
		ps.println("\t" + node.op().toString().toLowerCase() + " " + node.dest() + " " + node.arg());
	}

	@Override
	public void visit(TabNew node) {
		ps.println("\ttabnew " + node.dest() + " " + node.array() + " " + node.hash());
	}

	@Override
	public void visit(TabGet node) {
		ps.println("\ttabget " + node.dest() + " " + node.obj() + " " + node.key());
	}

	@Override
	public void visit(TabSet node) {
		ps.println("\ttabset " + node.dest() + " " + node.key() + " " + node.value());
	}

	@Override
	public void visit(TabStackAppend node) {
		ps.println("\ttabstackappend " + node.dest());
	}

	@Override
	public void visit(VarLoad node) {
		ps.println("\tvarload " + node.dest() + " " + node.var());
	}

	@Override
	public void visit(VarStore node) {
		ps.println("\tvarstore " + node.var() + " " + node.src());
	}

	@Override
	public void visit(UpLoad node) {
		ps.println("\tupload " + node.dest() + " " + node.upval());
	}

	@Override
	public void visit(UpStore node) {
		ps.println("\tupstore " + node.upval() + " " + node.src());
	}

	@Override
	public void visit(Vararg node) {
		ps.println("\tvararg");
	}

	private static String vlistToString(VList vl) {
		return "(" + (vl.isMulti() ? "multi" : "fixed") + " [" + Util.listToString(vl.addrs(), " ") + "])";
	}

	@Override
	public void visit(Ret node) {
		ps.println("\tret " + vlistToString(node.args()));
	}

	@Override
	public void visit(Call node) {
		ps.println("\tcall " + node.fn() + " " + vlistToString(node.args()));
	}

	@Override
	public void visit(TCall node) {
		ps.println("\ttcall " + node.target() + " " + vlistToString(node.args()));
	}

	@Override
	public void visit(StackGet node) {
		ps.println("\tstackget " + node.dest() + " " + node.idx());
	}

	@Override
	public void visit(Mov node) {
		ps.println("\tmov " + node.dest() + " " + node.src());
	}

	@Override
	public void visit(Label node) {
		ps.println(node + ":");
	}

	@Override
	public void visit(Jmp node) {
		ps.println("\tjmp " + node.jmpDest());
	}

	@Override
	public void visit(CJmp node) {
		ps.println("\tcjmp " + node.addr() + " " + node.expected() + " " + node.jmpDest());
	}

	@Override
	public void visit(Closure node) {
		ps.println("\tclosure " + node.dest() + " [" + Util.listToString(node.args(), " ") + "]");
	}

	@Override
	public void visit(ToNumber node) {
		ps.println("\ttonumber " + node.dest() + " " + node.src());
	}

	@Override
	public void visit(CheckForEnd node) {
		ps.println("\tcheckforend " + node.var() + " " + node.limit() + " " + node.step() + " " + node.jmpDest());
	}

	@Override
	public void visit(JmpIfNil node) {
		ps.println("\tjmpifnil " + node.addr() + " " + node.jmpDest());
	}

	@Override
	public void visit(ToNext node) {
		ps.println("\t; fall through to " + node.label());
	}

}

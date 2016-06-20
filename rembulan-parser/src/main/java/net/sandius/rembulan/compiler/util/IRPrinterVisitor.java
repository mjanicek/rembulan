package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.compiler.ir.BinOp;
import net.sandius.rembulan.compiler.ir.Call;
import net.sandius.rembulan.compiler.ir.Dup;
import net.sandius.rembulan.compiler.ir.IRVisitor;
import net.sandius.rembulan.compiler.ir.LoadConst;
import net.sandius.rembulan.compiler.ir.Ret;
import net.sandius.rembulan.compiler.ir.StackGet;
import net.sandius.rembulan.compiler.ir.TCall;
import net.sandius.rembulan.compiler.ir.TabGet;
import net.sandius.rembulan.compiler.ir.TabNew;
import net.sandius.rembulan.compiler.ir.TabSet;
import net.sandius.rembulan.compiler.ir.TabStackAppend;
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

public class IRPrinterVisitor extends IRVisitor {

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
		ps.println("\tvarstore " + node.var());
	}

	@Override
	public void visit(UpLoad node) {
		ps.println("\tupload " + node.dest() + " " + node.upval());
	}

	@Override
	public void visit(UpStore node) {
		ps.println("\tupstore " + node.upval());
	}

	@Override
	public void visit(Vararg node) {
		ps.println("\tvararg");
	}

	@Override
	public void visit(Dup node) {
		ps.println("\tdup");
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

}

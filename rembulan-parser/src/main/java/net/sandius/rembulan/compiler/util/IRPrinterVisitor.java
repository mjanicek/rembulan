package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.compiler.ir.ArrayGet;
import net.sandius.rembulan.compiler.ir.BinOp;
import net.sandius.rembulan.compiler.ir.Call;
import net.sandius.rembulan.compiler.ir.Dup;
import net.sandius.rembulan.compiler.ir.IRVisitor;
import net.sandius.rembulan.compiler.ir.LoadConst;
import net.sandius.rembulan.compiler.ir.Ret;
import net.sandius.rembulan.compiler.ir.StackGet;
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
		ps.println("(ldnil " + node.dest() + ")");
	}

	@Override
	public void visit(LoadConst.Bool node) {
		ps.println("(ldbool " + node.dest() + " " + node.value() + ")");
	}

	@Override
	public void visit(LoadConst.Int node) {
		ps.println("(ldint " + node.dest() + " " + node.value() + ")");
	}

	@Override
	public void visit(LoadConst.Flt node) {
		ps.println("(ldflt " + node.dest() + " " + node.value() + ")");
	}

	@Override
	public void visit(LoadConst.Str node) {
		ps.println("(ldstr " + node.dest() + " " + LuaFormat.escape(node.value()) + ")");
	}

	@Override
	public void visit(BinOp node) {
		ps.println("(" + node.op().toString().toLowerCase() + " " + node.dest() + " " + node.left() + " " + node.right() + ")");
	}

	@Override
	public void visit(UnOp node) {
		ps.println("(" + node.op().toString().toLowerCase() + " " + node.dest() + " " + node.arg() + ")");
	}

	@Override
	public void visit(TabNew node) {
		ps.println("(tabnew " + node.dest() + " " + node.array() + " " + node.hash() + ")");
	}

	@Override
	public void visit(TabGet node) {
		ps.println("(tabget " + node.dest() + " " + node.obj() + " " + node.key() + ")");
	}

	@Override
	public void visit(TabSet node) {
		ps.println("(tabset " + node.dest() + " " + node.key() + " " + node.value() + ")");
	}

	@Override
	public void visit(TabStackAppend node) {
		ps.println("(tabstackappend " + node.dest() + ")");
	}

	@Override
	public void visit(VarLoad node) {
		ps.println("(varload " + node.dest() + " " + node.var() + ")");
	}

	@Override
	public void visit(VarStore node) {
		ps.println("varstore " + node.var());
	}

	@Override
	public void visit(UpLoad node) {
		ps.println("(upload " + node.dest() + " " + node.upval() + ")");
	}

	@Override
	public void visit(UpStore node) {
		ps.println("upstore " + node.upval());
	}

	@Override
	public void visit(Vararg node) {
		ps.println("(vararg " + node.dest() + " " + node.idx() + ")");
	}

	@Override
	public void visit(ArrayGet node) {
		ps.println("arrayget " + node.index());
	}

	@Override
	public void visit(Dup node) {
		ps.println("dup");
	}

	@Override
	public void visit(Ret node) {
		ps.println("(ret " + Util.listToString(node.args(), " ") + ")");
	}

	@Override
	public void visit(Call node) {
		Check.isTrue(node.args() instanceof VList.Fixed);
		ps.println("(call " + node.fn() + " (fixed " + Util.listToString(((VList.Fixed) node.args()).addrs(), " ") + "))");
	}

	@Override
	public void visit(StackGet node) {
		ps.println("(stackget " + node.dest() + " " + node.idx() + ")");
	}

}

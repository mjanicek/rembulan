package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.compiler.ir.ArrayGet;
import net.sandius.rembulan.compiler.ir.BinOp;
import net.sandius.rembulan.compiler.ir.Dup;
import net.sandius.rembulan.compiler.ir.IRVisitor;
import net.sandius.rembulan.compiler.ir.LoadConst;
import net.sandius.rembulan.compiler.ir.TabGet;
import net.sandius.rembulan.compiler.ir.TabSet;
import net.sandius.rembulan.compiler.ir.UnOp;
import net.sandius.rembulan.compiler.ir.UpLoad;
import net.sandius.rembulan.compiler.ir.UpStore;
import net.sandius.rembulan.compiler.ir.VarLoad;
import net.sandius.rembulan.compiler.ir.VarStore;
import net.sandius.rembulan.compiler.ir.Vararg;
import net.sandius.rembulan.util.Check;

import java.io.PrintWriter;

public class IRPrinterVisitor extends IRVisitor {

	private final PrintWriter ps;

	public IRPrinterVisitor(PrintWriter ps) {
		this.ps = Check.notNull(ps);
	}

	@Override
	public void visit(LoadConst.Nil node) {
		ps.println("loadnil");
	}

	@Override
	public void visit(LoadConst.Bool node) {
		ps.println("loadbool " + node.value());
	}

	@Override
	public void visit(LoadConst.Int node) {
		ps.println("loadint " + node.value());
	}

	@Override
	public void visit(LoadConst.Flt node) {
		ps.println("loadflt " + node.value());
	}

	@Override
	public void visit(LoadConst.Str node) {
		ps.println("loadstr " + LuaFormat.escape(node.value()));
	}

	@Override
	public void visit(BinOp node) {
		ps.println("binop " + node.op());
	}

	@Override
	public void visit(UnOp node) {
		ps.println("unop " + node.op());
	}

	@Override
	public void visit(TabGet node) {
		ps.println("tabget");
	}

	@Override
	public void visit(TabSet node) {
		ps.println("tabset");
	}

	@Override
	public void visit(VarLoad node) {
		ps.println("varload " + node.var());
	}

	@Override
	public void visit(VarStore node) {
		ps.println("varstore " + node.var());
	}

	@Override
	public void visit(UpLoad node) {
		ps.println("upload " + node.upval());
	}

	@Override
	public void visit(UpStore node) {
		ps.println("upstore " + node.upval());
	}

	@Override
	public void visit(Vararg vararg) {
		ps.println("vararg");
	}

	@Override
	public void visit(ArrayGet node) {
		ps.println("arrayget " + node.index());
	}

	@Override
	public void visit(Dup node) {
		ps.println("dup");
	}

}

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

package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.parser.util.Util;

import java.io.PrintWriter;
import java.util.Objects;

public class IRPrinterVisitor extends CodeVisitor {

	private final PrintWriter ps;

	public IRPrinterVisitor(PrintWriter ps) {
		this.ps = Objects.requireNonNull(ps);
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
		ps.println("\ttabset " + node.obj() + " " + node.key() + " " + node.value());
	}

	@Override
	public void visit(TabRawSet node) {
		ps.println("\ttabrawset " + node.obj() + " " + node.key() + " " + node.value());
	}

	@Override
	public void visit(TabRawSetInt node) {
		ps.println("\ttabrawsetint " + node.obj() + " " + node.idx() + " " + node.value());
	}

	@Override
	public void visit(TabRawAppendMulti node) {
		ps.println("\ttabrawappendstack " + node.obj() + " " + node.src() + " " + node.firstIdx());
	}

	@Override
	public void visit(VarInit node) {
		ps.println("\tvarinit " + node.var() + " " + node.src());
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
		ps.println("\tvararg " + node.dest());
	}

	private static String vlistToString(VList vl) {
		return "(" + (vl.isMulti() ? "multi" : "fixed")
				+ " [" + Util.listToString(vl.addrs(), " ") + "]"
				+ (vl.suffix() != null ? " " + vl.suffix() : "") + ")";
	}

	@Override
	public void visit(Ret node) {
		ps.println("\tret " + vlistToString(node.args()));
	}

	@Override
	public void visit(Call node) {
		ps.println("\tcall " + node.dest() + " " + node.fn() + " " + vlistToString(node.args()));
	}

	@Override
	public void visit(TCall node) {
		ps.println("\ttcall " + node.target() + " " + vlistToString(node.args()));
	}

	@Override
	public void visit(MultiGet node) {
		ps.println("\tstackget " + node.dest() + " " + node.src() + " " + node.idx());
	}

	@Override
	public void visit(PhiStore node) {
		ps.println("\tphistore " + node.dest() + " " + node.src());
	}

	@Override
	public void visit(PhiLoad node) {
		ps.println("\tphiload " + node.dest() + " " + node.src());
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
	public void visit(Branch node) {
		ps.print("\tif (");
		node.condition().accept(this);
		ps.println(") " + node.jmpDest());
		ps.println("\t; else fall through to " + node.next());
	}

	@Override
	public void visit(Branch.Condition.Nil cond) {
		ps.print("nil " + cond.addr());
	}

	@Override
	public void visit(Branch.Condition.Bool cond) {
		ps.print(cond.expected() + " " + cond.addr());
	}

	@Override
	public void visit(Branch.Condition.NumLoopEnd cond) {
		ps.print("loopend " + cond.var() + " " + cond.limit() + " " + cond.step());
	}

	@Override
	public void visit(Closure node) {
		ps.println("\tclosure " + node.dest() + " " + node.id() + " [" + Util.listToString(node.args(), " ") + "]");
	}

	@Override
	public void visit(ToNumber node) {
		ps.println("\ttonumber " + node.dest() + " " + node.src() + (node.desc() != null ? " (" + node.desc() + ")" : ""));
	}

	@Override
	public void visit(ToNext node) {
		ps.println("\t; fall through to " + node.label());
	}

	@Override
	public void visit(CPUWithdraw node) {
		ps.println("\tcpu " + node.cost());
	}

	@Override
	public void visit(Line node) {
		ps.println("\t; line " + node.lineNumber());
	}

}

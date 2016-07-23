package net.sandius.rembulan.compiler.util;

import net.sandius.rembulan.compiler.ir.*;

public abstract class DefaultNodeActionVisitor extends IRVisitor {

	protected abstract void action(IRNode node);

	@Override
	public void visit(LoadConst.Nil node) {
		action(node);
	}

	@Override
	public void visit(LoadConst.Bool node) {
		action(node);
	}

	@Override
	public void visit(LoadConst.Int node) {
		action(node);
	}

	@Override
	public void visit(LoadConst.Flt node) {
		action(node);
	}

	@Override
	public void visit(LoadConst.Str node) {
		action(node);
	}

	@Override
	public void visit(BinOp node) {
		action(node);
	}

	@Override
	public void visit(UnOp node) {
		action(node);
	}

	@Override
	public void visit(TabNew node) {
		action(node);
	}

	@Override
	public void visit(TabGet node) {
		action(node);
	}

	@Override
	public void visit(TabSet node) {
		action(node);
	}

	@Override
	public void visit(TabRawAppendMulti node) {
		action(node);
	}

	@Override
	public void visit(VarInit node) {
		action(node);
	}

	@Override
	public void visit(VarLoad node) {
		action(node);
	}

	@Override
	public void visit(VarStore node) {
		action(node);
	}

	@Override
	public void visit(UpLoad node) {
		action(node);
	}

	@Override
	public void visit(UpStore node) {
		action(node);
	}

	@Override
	public void visit(Vararg node) {
		action(node);
	}

	@Override
	public void visit(Ret node) {
		action(node);
	}

	@Override
	public void visit(TCall node) {
		action(node);
	}

	@Override
	public void visit(Call node) {
		action(node);
	}

	@Override
	public void visit(MultiGet node) {
		action(node);
	}

	@Override
	public void visit(PhiStore node) {
		action(node);
	}

	@Override
	public void visit(PhiLoad node) {
		action(node);
	}

	@Override
	public void visit(Jmp node) {
		action(node);
	}

	@Override
	public void visit(Closure node) {
		action(node);
	}

	@Override
	public void visit(ToNumber node) {
		action(node);
	}

	@Override
	public void visit(ToNext node) {
		action(node);
	}

	@Override
	public void visit(Branch node) {
		action(node);
	}

	@Override
	public void visit(CPUWithdraw node) {
		action(node);
	}

}

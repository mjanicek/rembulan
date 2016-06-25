package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.ir.*;

public abstract class AbstractUseDefVisitor extends IRVisitor {

	protected abstract void def(Val v);
	protected abstract void use(Val v);
		
	protected abstract void def(PhiVal pv);
	protected abstract void use(PhiVal pv);
		
	protected abstract void def(Var v);
	protected abstract void use(Var v);

	protected abstract void def(UpVar uv);
	protected abstract void use(UpVar uv);

	@Override
	public void visit(LoadConst.Nil node) {
		def(node.dest());
	}

	@Override
	public void visit(LoadConst.Bool node) {
		def(node.dest());
	}

	@Override
	public void visit(LoadConst.Int node) {
		def(node.dest());
	}

	@Override
	public void visit(LoadConst.Flt node) {
		def(node.dest());
	}

	@Override
	public void visit(LoadConst.Str node) {
		def(node.dest());
	}

	@Override
	public void visit(BinOp node) {
		use(node.left());
		use(node.right());
		def(node.dest());
	}

	@Override
	public void visit(UnOp node) {
		use(node.arg());
		def(node.dest());
	}

	@Override
	public void visit(TabNew node) {
		def(node.dest());
	}

	@Override
	public void visit(TabGet node) {
		use(node.obj());
		use(node.key());
		def(node.dest());
	}

	@Override
	public void visit(TabSet node) {
		use(node.obj());
		use(node.key());
		use(node.value());
	}

	@Override
	public void visit(TabStackAppend node) {
		use(node.obj());
	}

	@Override
	public void visit(VarLoad node) {
		use(node.var());
		def(node.dest());
	}

	@Override
	public void visit(VarStore node) {
		use(node.src());
		def(node.var());
	}

	@Override
	public void visit(UpLoad node) {
		use(node.upval());
		def(node.dest());
	}

	@Override
	public void visit(UpStore node) {
		use(node.src());
		def(node.upval());
	}

	@Override
	public void visit(Vararg node) {
		// no effect
	}

	@Override
	public void visit(Ret node) {
		for (Val v : node.args().addrs()) {
			use(v);
		}
	}

	@Override
	public void visit(TCall node) {
		use(node.target());
		for (Val v : node.args().addrs()) {
			use(v);
		}
	}

	@Override
	public void visit(Call node) {
		use(node.fn());
		for (Val v : node.args().addrs()) {
			use(v);
		}
	}

	@Override
	public void visit(StackGet node) {
		def(node.dest());
	}

	@Override
	public void visit(PhiStore node) {
		use(node.src());
		def(node.dest());
	}

	@Override
	public void visit(PhiLoad node) {
		use(node.src());
		def(node.dest());
	}

	@Override
	public void visit(Label node) {
		// no effect
	}

	@Override
	public void visit(Jmp node) {
		// no effect
	}

	@Override
	public void visit(Closure node) {
		for (Var v : node.args()) {
			use(v);
		}
		def(node.dest());
	}

	@Override
	public void visit(ToNumber node) {
		use(node.src());
		def(node.dest());
	}

	@Override
	public void visit(ToNext node) {
		// no effect
	}

	@Override
	public void visit(Branch branch) {
		branch.condition().accept(this);
	}

	@Override
	public void visit(Branch.Condition.Nil cond) {
		use(cond.addr());
	}

	@Override
	public void visit(Branch.Condition.Bool cond) {
		use(cond.addr());
	}

	@Override
	public void visit(Branch.Condition.NumLoopEnd cond) {
		use(cond.var());
		use(cond.limit());
		use(cond.step());
	}

}

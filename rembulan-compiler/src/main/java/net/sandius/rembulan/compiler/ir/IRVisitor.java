package net.sandius.rembulan.compiler.ir;

public abstract class IRVisitor {

	private final IRVisitor v;

	public IRVisitor(IRVisitor v) {
		this.v = v;
	}

	public IRVisitor() {
		this(null);
	}


	public void visit(LoadConst.Nil node) {
		if (v != null) v.visit(node);
	}

	public void visit(LoadConst.Bool node) {
		if (v != null) v.visit(node);
	}

	public void visit(LoadConst.Int node) {
		if (v != null) v.visit(node);
	}

	public void visit(LoadConst.Flt node) {
		if (v != null) v.visit(node);
	}

	public void visit(LoadConst.Str node) {
		if (v != null) v.visit(node);
	}


	public void visit(BinOp node) {
		if (v != null) v.visit(node);
	}

	public void visit(UnOp node) {
		if (v != null) v.visit(node);
	}


	public void visit(TabNew node) {
		if (v != null) v.visit(node);
	}

	public void visit(TabGet node) {
		if (v != null) v.visit(node);
	}

	public void visit(TabSet node) {
		if (v != null) v.visit(node);
	}

	public void visit(TabRawSet node) {
		if (v != null) v.visit(node);
	}

	public void visit(TabRawSetInt node) {
		if (v != null) v.visit(node);
	}

	public void visit(TabRawAppendStack node) {
		if (v != null) v.visit(node);
	}


	public void visit(VarInit node) {
		if (v != null) v.visit(node);
	}

	public void visit(VarLoad node) {
		if (v != null) v.visit(node);
	}

	public void visit(VarStore node) {
		if (v != null) v.visit(node);
	}


	public void visit(UpLoad node) {
		if (v != null) v.visit(node);
	}

	public void visit(UpStore node) {
		if (v != null) v.visit(node);
	}

	public void visit(Vararg node) {
		if (v != null) v.visit(node);
	}


	public void visit(Ret node) {
		if (v != null) v.visit(node);
	}

	public void visit(TCall node) {
		if (v != null) v.visit(node);
	}

	public void visit(Call node) {
		if (v != null) v.visit(node);
	}


	public void visit(StackGet node) {
		if (v != null) v.visit(node);
	}

	public void visit(PhiStore node) {
		if (v != null) v.visit(node);
	}

	public void visit(PhiLoad node) {
		if (v != null) v.visit(node);
	}

	public void visit(Label node) {
		if (v != null) v.visit(node);
	}

	public void visit(Jmp node) {
		if (v != null) v.visit(node);
	}


	public void visit(Closure node) {
		if (v != null) v.visit(node);
	}

	public void visit(ToNumber node) {
		if (v != null) v.visit(node);
	}

	public void visit(ToNext node) {
		if (v != null) v.visit(node);
	}


	public void visit(Branch branch) {
		if (v != null) v.visit(branch);
	}

	public void visit(Branch.Condition.Nil cond) {
		if (v != null) v.visit(cond);
	}

	public void visit(Branch.Condition.Bool cond) {
		if (v != null) v.visit(cond);
	}

	public void visit(Branch.Condition.NumLoopEnd cond) {
		if (v != null) v.visit(cond);
	}

	public void visit(CPUWithdraw node) {
		if (v != null) v.visit(node);
	}

}

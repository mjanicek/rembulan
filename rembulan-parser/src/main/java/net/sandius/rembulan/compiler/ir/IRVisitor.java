package net.sandius.rembulan.compiler.ir;

public abstract class IRVisitor {

	public abstract void visit(LoadConst.Nil node);

	public abstract void visit(LoadConst.Bool node);

	public abstract void visit(LoadConst.Int node);

	public abstract void visit(LoadConst.Flt node);

	public abstract void visit(LoadConst.Str node);


	public abstract void visit(BinOp node);

	public abstract void visit(UnOp node);


	public abstract void visit(TabNew node);

	public abstract void visit(TabGet node);

	public abstract void visit(TabSet node);

	public abstract void visit(TabStackAppend node);


	public abstract void visit(VarLoad node);

	public abstract void visit(VarStore node);


	public abstract void visit(UpLoad node);

	public abstract void visit(UpStore node);

	public abstract void visit(Vararg node);


	public abstract void visit(Ret node);

	public abstract void visit(TCall node);

	public abstract void visit(Call node);


	public abstract void visit(StackGet node);

	public abstract void visit(PhiStore node);

	public abstract void visit(PhiLoad node);

	public abstract void visit(Label node);

	public abstract void visit(Jmp node);


	public abstract void visit(Closure node);

	public abstract void visit(ToNumber node);

	public abstract void visit(ToNext node);


	public abstract void visit(Branch branch);

	public abstract void visit(Branch.Condition.Nil cond);
	public abstract void visit(Branch.Condition.Bool cond);
	public abstract void visit(Branch.Condition.NumLoopEnd cond);

}

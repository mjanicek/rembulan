package net.sandius.rembulan.compiler.ir;

public abstract class IRVisitor {

	public void visit(LoadConst.Nil node) { }

	public void visit(LoadConst.Bool node) { }

	public void visit(LoadConst.Int node) { }

	public void visit(LoadConst.Flt node) { }

	public void visit(LoadConst.Str node) { }


	public void visit(BinOp node) { }

	public void visit(UnOp node) { }


	public void visit(TabGet node) { }

	public void visit(TabSet node) { }


	public void visit(VarLoad node) { }

	public void visit(VarStore node) { }


	public void visit(UpLoad node) { }

	public void visit(UpStore node) { }

	public void visit(Vararg node) { }


	public void visit(ArrayGet node) { }


	public void visit(Dup node) { }

}

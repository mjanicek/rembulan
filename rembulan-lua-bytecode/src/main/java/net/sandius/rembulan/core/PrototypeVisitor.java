package net.sandius.rembulan.core;

public abstract class PrototypeVisitor {

	public abstract void visit(int numParams, boolean vararg, int maxStackSize, String source, int firstLineDefined, int lastLineDefined);

	public abstract void visitEnd();

	public abstract void visitInstruction(int insn);

	public abstract void visitNilConst();

	public abstract void visitBooleanConst(boolean value);

	public abstract void visitIntegerConst(long value);

	public abstract void visitFloatConst(double value);

	public abstract void visitStringConst(String value);

	public abstract void visitUpvalue(boolean inStack, int index);

	public abstract PrototypeVisitor visitNestedPrototype();

	public abstract void visitLine(int line);

	public abstract void visitUpvalueName(String name);

	public abstract void visitLocalVariable(String name, int beginPC, int endPC);

}

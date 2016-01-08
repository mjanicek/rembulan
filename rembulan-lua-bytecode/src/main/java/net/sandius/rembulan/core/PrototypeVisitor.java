package net.sandius.rembulan.core;

public abstract class PrototypeVisitor {

	protected final PrototypeVisitor pv;

	public PrototypeVisitor(PrototypeVisitor pv) {
		this.pv = pv;
	}

	public PrototypeVisitor() {
		this(null);
	}

	public void visit(int numParams, boolean vararg, int maxStackSize, String source, int firstLineDefined, int lastLineDefined) {
		if (pv != null) {
			pv.visit(numParams, vararg, maxStackSize, source, firstLineDefined, lastLineDefined);
		}
	}

	public void visitEnd() {
		if (pv != null) {
			pv.visitEnd();
		}
	}

	public void visitInstruction(int insn) {
		if (pv != null) {
			pv.visitInstruction(insn);
		}
	}

	public void visitNilConst() {
		if (pv != null) {
			pv.visitNilConst();
		}
	}

	public void visitBooleanConst(boolean value) {
		if (pv != null) {
			pv.visitBooleanConst(value);
		}
	}

	public void visitIntegerConst(long value) {
		if (pv != null) {
			pv.visitIntegerConst(value);
		}
	}

	public void visitFloatConst(double value) {
		if (pv != null) {
			pv.visitFloatConst(value);
		}
	}

	public void visitStringConst(String value) {
		if (pv != null) {
			pv.visitStringConst(value);
		}
	}

	public void visitUpvalue(boolean inStack, int index) {
		if (pv != null) {
			pv.visitUpvalue(inStack, index);
		}
	}

	public PrototypeVisitor visitNestedPrototype() {
		if (pv != null) {
			return pv.visitNestedPrototype();
		}

		return null;
	}

	public void visitLine(int line) {
		if (pv != null) {
			pv.visitLine(line);
		}
	}

	public void visitUpvalueName(String name) {
		if (pv != null) {
			pv.visitUpvalueName(name);
		}
	}

	public void visitLocalVariable(String name, int beginPC, int endPC) {
		if (pv != null) {
			pv.visitLocalVariable(name, beginPC, endPC);
		}
	}

}

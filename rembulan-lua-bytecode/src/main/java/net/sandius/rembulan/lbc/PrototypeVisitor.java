package net.sandius.rembulan.lbc;

public abstract class PrototypeVisitor {

	protected final PrototypeVisitor pv;

	public PrototypeVisitor(PrototypeVisitor pv) {
		this.pv = pv;
	}

	public PrototypeVisitor() {
		this(null);
	}

	public void visitSource(String source, int firstLineDefined, int lastLineDefined) {
		if (pv != null) {
			pv.visitSource(source, firstLineDefined, lastLineDefined);
		}
	}

	public void visitSize(int numParams, boolean vararg, int maxStackSize) {
		if (pv != null) {
			pv.visitSize(numParams, vararg, maxStackSize);
		}
	}

	public void visitEnd() {
		if (pv != null) {
			pv.visitEnd();
		}
	}

	public void visitABCInstruction(int opcode, int a, int b, int c) {
		if (pv != null) {
			pv.visitABCInstruction(opcode, a, b, c);
		}
	}

	public void visitABxInstruction(int opcode, int a, int bx) {
		if (pv != null) {
			pv.visitABxInstruction(opcode, a, bx);
		}
	}

	public void visitAsBxInstruction(int opcode, int a, int sbx) {
		if (pv != null) {
			pv.visitAsBxInstruction(opcode, a, sbx);
		}
	}

	public void visitAxInstruction(int opcode, int ax) {
		if (pv != null) {
			pv.visitAxInstruction(opcode, ax);
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

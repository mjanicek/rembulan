package net.sandius.rembulan.core;

public class DebugStripPrototypeVisitor extends PrototypeVisitor {

	public DebugStripPrototypeVisitor(PrototypeVisitor pv) {
		super(pv);
	}

	public DebugStripPrototypeVisitor() {
		super();
	}

	@Override
	public PrototypeVisitor visitNestedPrototype() {
		if (pv != null) {
			PrototypeVisitor nested = pv.visitNestedPrototype();
			return new DebugStripPrototypeVisitor(nested);
		}
		return null;
	}

	@Override
	public void visitLine(int line) {
		// no-op
	}

	@Override
	public void visitUpvalueName(String name) {
		// no-op
	}

	@Override
	public void visitLocalVariable(String name, int beginPC, int endPC) {
		// no-op
	}

}

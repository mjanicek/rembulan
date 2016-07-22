package net.sandius.rembulan.compiler.tf;

import net.sandius.rembulan.compiler.IRFunc;

public class CPUAccounter {

	public static IRFunc insertCPUAccounting(IRFunc fn) {
		CPUAccountingVisitor visitor = new CPUAccountingVisitor(CPUAccountingVisitor.INITIALISE);
		visitor.visit(fn);
		return fn.update(visitor.result());
  	}

	public static IRFunc collectCPUAccounting(IRFunc fn) {
		CPUAccountingVisitor visitor = new CPUAccountingVisitor(CPUAccountingVisitor.COLLECT);
		visitor.visit(fn);
		return fn.update(visitor.result());
  	}

}

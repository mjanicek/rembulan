package net.sandius.rembulan.compiler.tf;

import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.analysis.TypeInfo;

public class BranchInliner {

	public static IRFunc inlineBranches(IRFunc fn, TypeInfo typeInfo) {
		BranchInlinerVisitor visitor = new BranchInlinerVisitor(typeInfo);
		visitor.visit(fn);
		return fn.update(visitor.result());
	}

}

package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.IRFunc;

public class DependencyAnalyser {

	public static DependencyInfo analyse(IRFunc fn) {
		NestedRefVisitor visitor = new NestedRefVisitor();
		visitor.visit(fn);
		return visitor.dependencyInfo();
	}

}

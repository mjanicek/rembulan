package net.sandius.rembulan.compiler.tf;

import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.analysis.LivenessInfo;
import net.sandius.rembulan.compiler.analysis.TypeInfo;

public class LivenessPruner {

	public static IRFunc pruneDeadCode(IRFunc fn, TypeInfo types, LivenessInfo liveness) {
		LivenessPrunerVisitor visitor = new LivenessPrunerVisitor(types, liveness);
		visitor.visit(fn);
		return fn.update(visitor.result());
	}

}

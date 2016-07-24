package net.sandius.rembulan.compiler.tf;

import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.analysis.TypeInfo;

public class ConstFolder {

	public static IRFunc replaceConstOperations(IRFunc fn, TypeInfo typeInfo) {
		ConstFolderVisitor visitor = new ConstFolderVisitor(typeInfo);
		visitor.visit(fn);
		return fn.update(visitor.result());
	}

}

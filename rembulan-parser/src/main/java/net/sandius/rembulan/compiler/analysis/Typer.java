package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.IRFunc;

public class Typer {

	public static TypeInfo analyseTypes(IRFunc fn) {
		TyperVisitor visitor = new TyperVisitor();
		visitor.visit(fn);
		return visitor.valTypes();
	}

}

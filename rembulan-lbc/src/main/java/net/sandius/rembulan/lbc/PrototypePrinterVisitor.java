package net.sandius.rembulan.lbc;

import net.sandius.rembulan.util.Check;

import java.io.PrintWriter;

public class PrototypePrinterVisitor extends PrototypeVisitor {

	protected final PrintWriter out;

	public PrototypePrinterVisitor(PrintWriter out) {
		super(new PrototypeBuilderVisitor());
		this.out = Check.notNull(out);
	}

	@Override
	public void visitEnd() {
		super.visitEnd();

		PrototypeBuilderVisitor bld = (PrototypeBuilderVisitor) pv;
		Prototype prototype = bld.get();

		PrototypePrinter.print(prototype, out);
	}

}

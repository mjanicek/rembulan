package net.sandius.rembulan.core;

import java.io.PrintWriter;
import java.util.Objects;

public class PrototypePrinterVisitor extends PrototypeVisitor {

	protected final PrintWriter out;

	public PrototypePrinterVisitor(PrintWriter out) {
		super(new PrototypeBuilderVisitor());
		this.out = Objects.requireNonNull(out);
	}

	@Override
	public void visitEnd() {
		super.visitEnd();

		PrototypeBuilderVisitor bld = (PrototypeBuilderVisitor) pv;
		Prototype prototype = bld.get();

		PrototypePrinter.print(prototype, out);
	}

}

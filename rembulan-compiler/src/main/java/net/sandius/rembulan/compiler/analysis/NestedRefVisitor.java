package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.FunctionId;
import net.sandius.rembulan.compiler.ir.Closure;
import net.sandius.rembulan.compiler.ir.CodeVisitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class NestedRefVisitor extends CodeVisitor {

	private final Set<FunctionId> ids;

	public NestedRefVisitor() {
		this.ids = new HashSet<>();
	}

	public DependencyInfo dependencyInfo() {
		return new DependencyInfo(Collections.unmodifiableSet(new HashSet<>(ids)));
	}

	@Override
	public void visit(Closure node) {
		ids.add(node.id());
	}

}

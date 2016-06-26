package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.BlocksVisitor;
import net.sandius.rembulan.compiler.FunctionId;
import net.sandius.rembulan.compiler.ir.Closure;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NestedRefVisitor extends BlocksVisitor {

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

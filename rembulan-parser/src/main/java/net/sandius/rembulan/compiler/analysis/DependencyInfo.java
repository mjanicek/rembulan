package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.compiler.FunctionId;
import net.sandius.rembulan.util.Check;

import java.util.Set;

public class DependencyInfo {

	private final Set<FunctionId> nestedRefs;

	public DependencyInfo(Set<FunctionId> nestedRefs) {
		this.nestedRefs = Check.notNull(nestedRefs);
	}

	public Set<FunctionId> nestedRefs() {
		return nestedRefs;
	}

}

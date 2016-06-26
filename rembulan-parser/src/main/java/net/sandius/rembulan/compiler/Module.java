package net.sandius.rembulan.compiler;

import net.sandius.rembulan.util.Check;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Module {

	private final List<IRFunc> fns;

	public Module(List<IRFunc> fns) {
		this.fns = Check.notNull(fns);
		verify();
	}

	private void verify() {
		Set<FunctionId> ids = new HashSet<>();
		for (IRFunc fn : fns) {
			if (!ids.add(fn.id())) {
				throw new IllegalStateException("Function " + fn.id() + " defined more than once");
			}
		}
	}

	public List<IRFunc> fns() {
		return fns;
	}

}

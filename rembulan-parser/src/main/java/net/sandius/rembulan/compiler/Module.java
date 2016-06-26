package net.sandius.rembulan.compiler;

import net.sandius.rembulan.util.Check;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class Module {

	private final List<IRFunc> fns;

	public Module(List<IRFunc> fns) {
		this.fns = Check.notNull(fns);
		verify();
	}

	private void verify() {
		Set<FunctionId> ids = new HashSet<>();
		boolean hasMain = false;
		for (IRFunc fn : fns) {
			if (!ids.add(fn.id())) {
				throw new IllegalStateException("Function " + fn.id() + " defined more than once");
			}
			if (fn.id().isRoot()) {
				hasMain = true;
			}
		}
		if (!hasMain) {
			throw new IllegalStateException("No main function in module");
		}
	}

	public List<IRFunc> fns() {
		return fns;
	}

	public IRFunc get(FunctionId id) {
		Check.notNull(id);

		for (IRFunc fn : fns) {
			if (fn.id().equals(id)) {
				return fn;
			}
		}

		throw new NoSuchElementException();
	}

	public IRFunc main() {
		return get(FunctionId.root());
	}

}

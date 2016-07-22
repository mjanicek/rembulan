package net.sandius.rembulan.compiler;

import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleBuilder {

	private final List<IRFunc> fns;

	public ModuleBuilder() {
		this.fns = new ArrayList<>();
	}

	public void add(IRFunc fn) {
		fns.add(Check.notNull(fn));
	}

	public Module build() {
		return new Module(Collections.unmodifiableList(fns));
	}

}

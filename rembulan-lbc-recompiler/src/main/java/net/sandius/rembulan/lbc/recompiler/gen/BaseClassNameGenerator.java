package net.sandius.rembulan.lbc.recompiler.gen;

import net.sandius.rembulan.util.Check;

public class BaseClassNameGenerator implements ClassNameGenerator {

	private final String name;

	public BaseClassNameGenerator(String name) {
		this.name = Check.notNull(name);
	}

	@Override
	public String next() {
		return name;
	}

	@Override
	public ClassNameGenerator childGenerator() {
		return new ChildClassNameGenerator(name);
	}
}

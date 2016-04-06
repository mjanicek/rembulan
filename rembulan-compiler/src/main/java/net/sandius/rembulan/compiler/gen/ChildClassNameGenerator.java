package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

public class ChildClassNameGenerator implements ClassNameGenerator {

	private final String base;
	private int idx;

	public ChildClassNameGenerator(String base) {
		this.base = Check.notNull(base);
		this.idx = 0;
	}

	private String childName(int i) {
		return "f" + i;
	}

	private String current() {
		return base + "$" + childName(idx);
	}

	@Override
	public String next() {
		idx++;
		return current();
	}

	@Override
	public ClassNameGenerator childGenerator() {
		return new ChildClassNameGenerator(current());
	}

}

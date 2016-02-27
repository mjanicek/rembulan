package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

public class SuffixingClassNameGenerator implements ClassNameGenerator {

	private final String base;

	public SuffixingClassNameGenerator(String base) {
		this.base = Check.notNull(base);
	}

	@Override
	public String className() {
		return base;
	}

	@Override
	public ClassNameGenerator child(int idx) {
		return new SuffixingClassNameGenerator(base + "$" + idx);
	}

}

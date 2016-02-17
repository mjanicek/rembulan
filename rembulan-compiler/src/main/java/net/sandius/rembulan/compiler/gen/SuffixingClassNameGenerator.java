package net.sandius.rembulan.compiler.gen;

import java.util.Objects;

public class SuffixingClassNameGenerator implements ClassNameGenerator {

	private final String base;

	public SuffixingClassNameGenerator(String base) {
		this.base = Objects.requireNonNull(base);
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

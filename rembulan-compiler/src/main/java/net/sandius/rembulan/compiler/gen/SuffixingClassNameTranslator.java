package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

public class SuffixingClassNameTranslator implements ClassNameTranslator {

	private final String base;

	public SuffixingClassNameTranslator(String base) {
		this.base = Check.notNull(base);
	}

	@Override
	public String className() {
		return base;
	}

	@Override
	public ClassNameTranslator child(int idx) {
		return new SuffixingClassNameTranslator(base + "$" + idx);
	}

}

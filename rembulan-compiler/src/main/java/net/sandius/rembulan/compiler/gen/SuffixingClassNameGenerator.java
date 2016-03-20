package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.util.Check;

import java.util.concurrent.atomic.AtomicInteger;

public class SuffixingClassNameGenerator implements ClassNameGenerator {

	private final String base;
	private final AtomicInteger idx;

	public SuffixingClassNameGenerator(String base) {
		this.base = Check.notNull(base);
		this.idx = new AtomicInteger(0);
	}

	@Override
	public String next() {
		int i = idx.incrementAndGet();
		return base + "$" + i;
	}

	@Override
	public ClassNameGenerator childGenerator() {
		return new SuffixingClassNameGenerator(base + "$" + idx.get());
	}

}

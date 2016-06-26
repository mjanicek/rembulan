package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.gen.CompiledClass;
import net.sandius.rembulan.util.Check;

import java.util.List;

public class CompiledModule extends AbstractChunk {

	private final List<CompiledClass> classes;
	private final String mainClassName;

	public CompiledModule(List<CompiledClass> classes, String mainClassName) {
		this.classes = Check.notNull(classes);
		this.mainClassName = Check.notNull(mainClassName);
	}

	@Override
	public Iterable<CompiledClass> classes() {
		return classes;
	}

	@Override
	public String mainClassName() {
		return mainClassName;
	}

}

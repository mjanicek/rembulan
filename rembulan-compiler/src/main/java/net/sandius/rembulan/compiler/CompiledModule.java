package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.gen.CompiledClass;
import net.sandius.rembulan.util.ByteVector;
import net.sandius.rembulan.util.Check;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompiledModule extends AbstractChunk {

	private final Map<String, ByteVector> classMap;
	private final String mainClassName;

	public CompiledModule(Map<String, ByteVector> classMap, String mainClassName) {
		this.classMap = Check.notNull(classMap);
		this.mainClassName = Check.notNull(mainClassName);

		if (!classMap.containsKey(mainClassName)) {
			throw new IllegalStateException("No main class in class map");
		}
	}

	@Override
	public Map<String, ByteVector> classMap() {
		return classMap;
	}

	@Override
	public Iterable<CompiledClass> classes() {
		List<CompiledClass> result = new ArrayList<>();
		for (Map.Entry<String, ByteVector> e : classMap.entrySet()) {
			result.add(new CompiledClass(e.getKey(), e.getValue()));
		}
		return result;
	}

	@Override
	public String mainClassName() {
		return mainClassName;
	}

}

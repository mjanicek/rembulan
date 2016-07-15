package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.gen.CompiledClass;
import net.sandius.rembulan.util.ByteVector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractChunk {

	@Deprecated
	public abstract Iterable<CompiledClass> classes();

	public Map<String, ByteVector> classMap() {
		Map<String, ByteVector> m = new HashMap<>();
		for (CompiledClass cc : classes()) {
			m.put(cc.name(), cc.bytes());
		}
		return Collections.unmodifiableMap(m);
	}

	public abstract String mainClassName();

}

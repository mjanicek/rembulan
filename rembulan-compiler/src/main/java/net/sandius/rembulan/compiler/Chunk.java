package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.gen.CompilationUnit;
import net.sandius.rembulan.compiler.gen.CompiledClass;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.ByteVector;
import net.sandius.rembulan.util.Check;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chunk {

	private final Prototype prototype;
	private final Map<Prototype, CompilationUnit> units;
	private final List<CompiledClass> classes;

	public Chunk(Prototype prototype, Map<Prototype, CompilationUnit> units, List<CompiledClass> classes) {
		this.prototype = Check.notNull(prototype);
		this.units = Collections.unmodifiableMap(Check.notNull(units));
		this.classes = Collections.unmodifiableList(Check.notNull(classes));
	}

	public Prototype prototype() {
		return prototype;
	}

	public Iterable<CompilationUnit> units() {
		return Collections.unmodifiableCollection(units.values());
	}

	// Returns classes defined in this chunk ordered in such a way that all classes are
	// preceded by their dependencies (i.e. it's post-order).
	public Iterable<CompiledClass> classes() {
		return Collections.unmodifiableList(classes);
	}

	public Map<String, ByteVector> classMap() {
		Map<String, ByteVector> m = new HashMap<>();
		for (CompiledClass cc : classes()) {
			m.put(cc.name(), cc.bytes());
		}
		return Collections.unmodifiableMap(m);
	}

	public String mainClassName() {
		throw new UnsupportedOperationException();  // TODO
	}

}

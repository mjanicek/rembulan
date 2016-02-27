package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.Prototype;

public class ChunkCompiler {

	public CompiledChunk compile(Prototype prototype, ClassNameGenerator nameGenerator) {
		CompiledChunk chunk = new CompiledChunk(prototype, nameGenerator);
		chunk.go();
		return chunk;
	}

}

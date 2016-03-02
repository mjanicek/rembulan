package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.Prototype;

public class ChunkCompiler {

	public Chunk compile(Prototype prototype, ClassNameGenerator nameGenerator) {
		Chunk chunk = new Chunk(prototype, nameGenerator);
		chunk.go();
		return chunk;
	}

}

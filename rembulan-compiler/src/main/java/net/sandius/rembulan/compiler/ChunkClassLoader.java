package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.gen.Chunk;
import net.sandius.rembulan.compiler.gen.CompiledClass;

public class ChunkClassLoader extends ClassLoader {

	public Class<?> install(Chunk chunk) {
		Class<?> last = null;

		for (CompiledClass ccl : chunk.classes()) {
			last = defineClass(ccl);
		}

		return last;
	}

	public Class<?> defineClass(CompiledClass ccl) {
		byte[] bytes = ccl.bytes().copyToNewArray();
		return defineClass(ccl.name(), bytes, 0, bytes.length);
	}

}

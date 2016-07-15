package net.sandius.rembulan.compiler;

import net.sandius.rembulan.core.ChunkLoader;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LoaderException;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.parser.ParseException;

public class CompilerChunkLoader extends ChunkLoader {

	private final ChunkClassLoader chunkClassLoader;

	private int idx;

	public CompilerChunkLoader(ClassLoader classLoader) {
		this.chunkClassLoader = new ChunkClassLoader(classLoader);
		this.idx = 0;
	}

	@Override
	public Function loadTextChunk(Upvalue env, String chunkName, String sourceText) throws LoaderException {
		try {
			Compiler compiler = new Compiler();
			CompiledModule result = compiler.compile(sourceText, "stdin", "f" + (idx++));  // FIXME

			String mainClassName = chunkClassLoader.install(result);
			Class<?> clazz = chunkClassLoader.loadClass(mainClassName);

			return (Function) clazz.getConstructor(Upvalue.class).newInstance(env);
		}
		catch (ParseException | RuntimeException | LinkageError | ReflectiveOperationException ex) {
			throw new LoaderException(ex);
		}
	}

	@Override
	public Function loadBinaryChunk(Upvalue env, String chunkName, byte[] bytes, int offset, int len) throws LoaderException {
		throw new UnsupportedOperationException();  // TODO
	}

}

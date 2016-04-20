package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.gen.ChunkCompiler;
import net.sandius.rembulan.core.ChunkLoader;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LoaderException;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.lbc.PrototypeReader;
import net.sandius.rembulan.lbc.PrototypeReaderException;
import net.sandius.rembulan.util.Check;

public class PrototypeCompilerChunkLoader extends ChunkLoader {

	private final PrototypeReader reader;
	private final ChunkClassLoader chunkClassLoader;

	public PrototypeCompilerChunkLoader(PrototypeReader reader, ClassLoader classLoader) {
		this.reader = Check.notNull(reader);
		this.chunkClassLoader = new ChunkClassLoader(classLoader);
	}

	@Override
	public Function loadTextChunk(Upvalue env, String chunkName, String chunk) throws LoaderException {
		Check.notNull(env);
		try {
			Prototype proto = reader.load(chunk);
			if (proto == null) {
				throw new NullPointerException("Prototype is null");
			}

			ChunkCompiler compiler = new ChunkCompiler();
			Chunk chk = compiler.compile(proto, chunkName);

			String mainClassName = chunkClassLoader.install(chk);
			Class<?> clazz = chunkClassLoader.loadClass(mainClassName);

     		return (Function) clazz.getConstructor(Upvalue.class).newInstance(env);
		}
		catch (PrototypeReaderException
				| RuntimeException | LinkageError | ReflectiveOperationException ex) {
			throw new LoaderException(ex);
		}
	}

	@Override
	public Function loadBinaryChunk(Upvalue env, String chunkName, byte[] chunk, int offset, int len) throws LoaderException {
		throw new UnsupportedOperationException();  // TODO
	}

}

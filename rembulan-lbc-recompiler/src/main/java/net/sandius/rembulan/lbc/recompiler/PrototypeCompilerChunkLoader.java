package net.sandius.rembulan.lbc.recompiler;

import net.sandius.rembulan.compiler.ChunkClassLoader;
import net.sandius.rembulan.core.ChunkLoader;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LoaderException;
import net.sandius.rembulan.core.Variable;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.lbc.PrototypeReader;
import net.sandius.rembulan.lbc.PrototypeReaderException;
import net.sandius.rembulan.lbc.recompiler.gen.ChunkCompiler;
import net.sandius.rembulan.util.Check;

public class PrototypeCompilerChunkLoader extends ChunkLoader {

	private final PrototypeReader reader;
	private final ChunkClassLoader chunkClassLoader;
	private final ChunkCompiler.CPUAccountingCompilationMode cpuAccountingCompilationMode;

	public PrototypeCompilerChunkLoader(PrototypeReader reader, ClassLoader classLoader, ChunkCompiler.CPUAccountingCompilationMode cpuAccountingCompilationMode) {
		this.reader = Check.notNull(reader);
		this.chunkClassLoader = new ChunkClassLoader(classLoader);
		this.cpuAccountingCompilationMode = Check.notNull(cpuAccountingCompilationMode);
	}

	public PrototypeCompilerChunkLoader(PrototypeReader reader, ClassLoader classLoader) {
		this(reader, classLoader, ChunkCompiler.DEFAULT_MODE);
	}

	@Override
	public Function loadTextChunk(Variable env, String chunkName, String chunk) throws LoaderException {
		Check.notNull(env);
		try {
			Prototype proto = reader.load(chunk);
			if (proto == null) {
				throw new NullPointerException("Prototype is null");
			}

			ChunkCompiler compiler = new ChunkCompiler(cpuAccountingCompilationMode);
			Chunk chk = compiler.compile(proto, chunkName);

			String mainClassName = chunkClassLoader.install(chk);
			Class<?> clazz = chunkClassLoader.loadClass(mainClassName);

     		return (Function) clazz.getConstructor(Variable.class).newInstance(env);
		}
		catch (PrototypeReaderException
				| RuntimeException | LinkageError | ReflectiveOperationException ex) {
			throw new LoaderException(ex);
		}
	}

	@Override
	public Function loadBinaryChunk(Variable env, String chunkName, byte[] chunk, int offset, int len) throws LoaderException {
		throw new UnsupportedOperationException();  // TODO
	}

}

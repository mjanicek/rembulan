package net.sandius.rembulan.compiler;

import net.sandius.rembulan.core.ChunkLoader;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LoaderException;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.parser.ParseException;
import net.sandius.rembulan.util.Check;

public class CompilerChunkLoader extends ChunkLoader {

	private final ChunkClassLoader chunkClassLoader;
	private final Compiler compiler;

	private int idx;

	public CompilerChunkLoader(ClassLoader classLoader, Compiler compiler) {
		this.chunkClassLoader = new ChunkClassLoader(classLoader);
		this.compiler = Check.notNull(compiler);
		this.idx = 0;
	}

	public CompilerChunkLoader(ClassLoader classLoader, Compiler.CPUAccountingMode cpuAccountingMode) {
		this(classLoader, new Compiler(cpuAccountingMode));
	}

	public CompilerChunkLoader(ClassLoader classLoader) {
		this(classLoader, new Compiler());
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

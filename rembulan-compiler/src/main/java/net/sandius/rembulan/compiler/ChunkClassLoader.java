package net.sandius.rembulan.compiler;

import net.sandius.rembulan.util.ByteVector;

import java.util.HashMap;
import java.util.Map;

public class ChunkClassLoader extends ClassLoader {

	private final Map<String, ByteVector> installed;

	public ChunkClassLoader(ClassLoader parent) {
		super(parent);
		this.installed = new HashMap<>();
	}

	public ChunkClassLoader() {
		this(ChunkClassLoader.class.getClassLoader());
	}

	public String install(AbstractChunk chunk) {
		Map<String, ByteVector> classes = chunk.classMap();

		for (String name : classes.keySet()) {
			if (installed.containsKey(name)) {
				// class already installed
				throw new IllegalArgumentException();  // TODO: throw the right exception
			}

			installed.put(name, classes.get(name));
		}

		String main = chunk.mainClassName();
		assert (installed.containsKey(main));
		return main;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		ByteVector bv = installed.get(name);

		if (bv != null) {
			// no need to keep it any longer (TODO: thread-safety)
			installed.remove(name);
			return defineClass(name, bv);
		}
		else {
			throw new ClassNotFoundException(name);
		}
	}

	private Class<?> defineClass(String name, ByteVector bytes) {
		byte[] byteArray = bytes.copyToNewArray();
		return defineClass(name, byteArray, 0, byteArray.length);
	}

}

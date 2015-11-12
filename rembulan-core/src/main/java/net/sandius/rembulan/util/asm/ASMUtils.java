package net.sandius.rembulan.util.asm;

import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;

public abstract class ASMUtils {

	private ASMUtils() {
		// not to be instantiated
	}

	public static Type arrayTypeFor(Class<?> clazz) {
		return arrayTypeFor(clazz, 1);
	}

	public static Type arrayTypeFor(Class<?> clazz, int dimensions) {
		Check.notNull(clazz);
		if (dimensions < 1) {
			throw new IllegalArgumentException("dimensions must be at least 1");
		}

		String prefix = "[";
		for (int i = 1; i < dimensions; i++) prefix = prefix + "[";

		return Type.getType(prefix + Type.getType(clazz).getDescriptor());
	}

	public static Type typeForClassName(String className) {
		Check.notNull(className);
		return Type.getType("L" + className.replace(".", "/") + ";");
	}

}

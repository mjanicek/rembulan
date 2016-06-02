package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.LNumber;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public abstract class NumberMethods {

	private NumberMethods() {
		// not to be instantiated
	}

	public static AbstractInsnNode unary(Class clazz, Class resultClazz, String methodName) {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(clazz),
				methodName,
				Type.getMethodDescriptor(
						Type.getType(resultClazz)),
				false);
	}

	public static AbstractInsnNode binary(Class clazz, Class resultClazz, String methodName) {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(clazz),
				methodName,
				Type.getMethodDescriptor(
						Type.getType(resultClazz),
						Type.getType(LNumber.class)),
				false);
	}

}

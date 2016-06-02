package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.LNumber;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public abstract class NumberMethods {

	private NumberMethods() {
		// not to be instantiated
	}

	public static AbstractInsnNode unary(String methodName) {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(LNumber.class),
				methodName,
				Type.getMethodDescriptor(
						Type.getType(LNumber.class),
						Type.getType(LNumber.class)),
				false);
	}

	public static AbstractInsnNode binary(String methodName) {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(LNumber.class),
				methodName,
				Type.getMethodDescriptor(
						Type.getType(LNumber.class),
						Type.getType(LNumber.class),
						Type.getType(LNumber.class)),
				false);
	}

	public static AbstractInsnNode toInteger(Class clazz) {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(LNumber.class),
				"toInteger",
				Type.getMethodDescriptor(
						Type.getType(LNumber.class)),
				false);
	}

	public static AbstractInsnNode toFloat() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(LNumber.class),
				"toFloat",
				Type.getMethodDescriptor(
						Type.getType(LNumber.class)),
				false);
	}

}

package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.RawOperators;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class OperatorMethods {

	private OperatorMethods() {
		// not to be instantiated
	}

	public static AbstractInsnNode rawBinaryOperator(String methodName, Type returnType, Type argType) {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(RawOperators.class),
				methodName,
				Type.getMethodDescriptor(
						returnType,
						argType,
						argType),
				false);
	}

	public static AbstractInsnNode stringLen() {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(RawOperators.class),
				"stringLen",
				Type.getMethodDescriptor(
						Type.INT_TYPE,
						Type.getType(String.class)),
				false);
	}

}

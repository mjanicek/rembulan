package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.Upvalue;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class UpvalueMethods {

	private UpvalueMethods() {
		// not to be instantiated
	}

	public static AbstractInsnNode get() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(Upvalue.class),
				"get",
				Type.getMethodDescriptor(
						Type.getType(Object.class)),
				false);
	}

	public static AbstractInsnNode set() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(Upvalue.class),
				"set",
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						Type.getType(Object.class)),
				false);
	}

}

package net.sandius.rembulan.compiler.gen.asm.helpers;

import net.sandius.rembulan.core.Variable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class VariableMethods {

	private VariableMethods() {
		// not to be instantiated
	}

	public static Type selfTpe() {
		return Type.getType(Variable.class);
	}

	public static AbstractInsnNode constructor() {
		return new MethodInsnNode(
				INVOKESPECIAL,
				selfTpe().getInternalName(),
				"<init>",
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						Type.getType(Object.class)),
				false);
	}

	public static AbstractInsnNode get() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(Variable.class),
				"get",
				Type.getMethodDescriptor(
						Type.getType(Object.class)),
				false);
	}

	public static AbstractInsnNode set() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(Variable.class),
				"set",
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						Type.getType(Object.class)),
				false);
	}

}

package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.impl.Varargs;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class UtilMethods {

	private UtilMethods() {
		// not to be instantiated
	}

	public static AbstractInsnNode concatenateArrays() {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Varargs.class),
				"concat",
				Type.getMethodDescriptor(
						ASMUtils.arrayTypeFor(Object.class),
						ASMUtils.arrayTypeFor(Object.class),
						ASMUtils.arrayTypeFor(Object.class)),
				false);
	}

	public static InsnList getArrayElementOrNull(int index) {
		InsnList il = new InsnList();

		il.add(ASMUtils.loadInt(index));
		il.add(new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Varargs.class),
				"getElement",
				Type.getMethodDescriptor(
						Type.getType(Object.class),
						ASMUtils.arrayTypeFor(Object.class),
						Type.INT_TYPE),
				false));

		return il;
	}

	public static InsnList arrayFrom(int index) {
		InsnList il = new InsnList();

		il.add(ASMUtils.loadInt(index));
		il.add(new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Varargs.class),
				"from",
				Type.getMethodDescriptor(
						ASMUtils.arrayTypeFor(Object.class),
						ASMUtils.arrayTypeFor(Object.class),
						Type.INT_TYPE),
				false));

		return il;
	}

	public static AbstractInsnNode StringBuilder_append(Type t) {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(StringBuilder.class),
				"append",
				Type.getMethodDescriptor(
						Type.getType(StringBuilder.class),
						t),
				false);
	}

	public static AbstractInsnNode StringBuilder_toString() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(StringBuilder.class),
				"toString",
				Type.getMethodDescriptor(
						Type.getType(String.class)),
				false);
	}

	public static AbstractInsnNode String_compareTo() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(String.class),
				"compareTo",
				Type.getMethodDescriptor(
						Type.INT_TYPE,
						Type.getType(String.class)),
				false);
	}

}

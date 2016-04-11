package net.sandius.rembulan.util.asm;

import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.*;

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

	public static AbstractInsnNode loadInt(int i) {
		switch (i) {
			case -1: return new InsnNode(ICONST_M1);
			case 0:  return new InsnNode(ICONST_0);
			case 1:  return new InsnNode(ICONST_1);
			case 2:  return new InsnNode(ICONST_2);
			case 3:  return new InsnNode(ICONST_3);
			case 4:  return new InsnNode(ICONST_4);
			case 5:  return new InsnNode(ICONST_5);
			default: {
				if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) return new IntInsnNode(BIPUSH, i);
				else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) return new IntInsnNode(SIPUSH, i);
				else return new LdcInsnNode(i);
			}
		}
	}

	public static AbstractInsnNode loadLong(long l) {
		if (l == 0L) return new InsnNode(LCONST_0);
		else if (l == 1L) return new InsnNode(LCONST_1);
		else return new LdcInsnNode(l);
	}

	public static AbstractInsnNode loadDouble(double d) {
		if (d == 0.0) return new InsnNode(DCONST_0);
		else if (d == 1.0) return new InsnNode(DCONST_1);
		else return new LdcInsnNode(d);
	}

	public static AbstractInsnNode loadBoxedBoolean(boolean value) {
		return new FieldInsnNode(
				GETSTATIC,
				Type.getInternalName(Boolean.class),
				value ? "TRUE" : "FALSE",
				Type.getDescriptor(Boolean.class));
	}

	public static MethodInsnNode box(Type from, Type to) {
		return new MethodInsnNode(
				INVOKESTATIC,
				to.getInternalName(),
				"valueOf",
				Type.getMethodDescriptor(
						to,
						from),
				false);
	}

	public static MethodInsnNode ctor(Type of, Type... args) {
		return new MethodInsnNode(
				INVOKESPECIAL,
				of.getInternalName(),
				"<init>",
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						args),
				false);
	}

	public static MethodInsnNode ctor(Class clazz, Class... args) {
		Type[] argTypes = new Type[args.length];
		for (int i = 0; i < args.length; i++) {
			argTypes[i] = Type.getType(args[i]);
		}
		return ctor(Type.getType(clazz), argTypes);
	}

}

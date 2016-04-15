package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class BoxedPrimitivesMethods {

	private BoxedPrimitivesMethods() {
		// not to be instantiated
	}

	public static AbstractInsnNode loadNull() {
		return new InsnNode(ACONST_NULL);
	}

	public static AbstractInsnNode loadBoxedBoolean(boolean value) {
		return new FieldInsnNode(
				GETSTATIC,
				Type.getInternalName(Boolean.class),
				value ? "TRUE" : "FALSE",
				Type.getDescriptor(Boolean.class));
	}

	public static AbstractInsnNode booleanValue() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(Boolean.class),
				"booleanValue",
				Type.getMethodDescriptor(
						Type.BOOLEAN_TYPE),
				false);
	}

	public static AbstractInsnNode intValue(Class clazz) {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(clazz),
				"intValue",
				Type.getMethodDescriptor(
						Type.INT_TYPE),
				false);
	}

	public static AbstractInsnNode longValue(Class clazz) {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(clazz),
				"longValue",
				Type.getMethodDescriptor(
						Type.LONG_TYPE),
				false);
	}

	public static AbstractInsnNode doubleValue(Class clazz) {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(clazz),
				"doubleValue",
				Type.getMethodDescriptor(
						Type.DOUBLE_TYPE),
				false);
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

	public static AbstractInsnNode unbox(Class clazz, Type requiredType) {
		if (requiredType.equals(Type.LONG_TYPE)) {
			return BoxedPrimitivesMethods.longValue(clazz);
		}
		else if (requiredType.equals(Type.INT_TYPE)) {
			return BoxedPrimitivesMethods.intValue(clazz);
		}
		else if (requiredType.equals(Type.DOUBLE_TYPE)) {
			return BoxedPrimitivesMethods.doubleValue(clazz);
		}
		else {
			throw new UnsupportedOperationException("Unsupported primitive type: " + requiredType);
		}
	}

	public static InsnList loadBoxedConstant(Object k, Class<?> castTo) {
		InsnList il = new InsnList();

		if (k == null) {
			il.add(loadNull());
		}
		else if (k instanceof Boolean) {
			il.add(BoxedPrimitivesMethods.loadBoxedBoolean((Boolean) k));
		}
		else if (k instanceof Double || k instanceof Float) {
			il.add(ASMUtils.loadDouble(((Number) k).doubleValue()));
			il.add(BoxedPrimitivesMethods.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
		}
		else if (k instanceof Number) {
			il.add(ASMUtils.loadLong(((Number) k).longValue()));
			il.add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, Type.getType(Long.class)));
		}
		else if (k instanceof String) {
			il.add(new LdcInsnNode(k));
		}
		else {
			throw new UnsupportedOperationException("Illegal constant type: " + k.getClass());
		}

		if (castTo != null) {
			Check.notNull(k);
			if (!castTo.isAssignableFrom(k.getClass())) {
				il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(castTo)));
			}
		}

		return il;
	}

	public static InsnList loadBoxedConstant(Object k) {
		return loadBoxedConstant(k, null);
	}

	public static AbstractInsnNode loadNumericValue(Number n, Type requiredType) {
		if (n instanceof Double || n instanceof Float) {
			if (requiredType.equals(Type.LONG_TYPE)) return ASMUtils.loadLong(n.longValue());
			else if (requiredType.equals(Type.INT_TYPE)) return ASMUtils.loadInt(n.intValue());
			else if (requiredType.equals(Type.DOUBLE_TYPE)) return ASMUtils.loadDouble(n.doubleValue());
			else {
				throw new UnsupportedOperationException("Unsupported required type: " + requiredType);
			}
		}
		else {
			if (requiredType.equals(Type.LONG_TYPE)) return ASMUtils.loadLong(n.longValue());
			else if (requiredType.equals(Type.INT_TYPE)) return ASMUtils.loadInt(n.intValue());
			else if (requiredType.equals(Type.DOUBLE_TYPE)) return ASMUtils.loadDouble(n.doubleValue());
			else {
				throw new UnsupportedOperationException("Unsupported required type: " + requiredType);
			}
		}
	}

}

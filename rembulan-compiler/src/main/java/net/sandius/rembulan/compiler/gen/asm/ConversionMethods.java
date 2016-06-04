package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public abstract class ConversionMethods {

	private ConversionMethods() {
		// not to be instantiated or extended
	}

	public static InsnList toNumericalValue(String what) {
		Check.notNull(what);
		InsnList il = new InsnList();

		il.add(new LdcInsnNode(what));
		il.add(new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Conversions.class),
				"toNumericalValue",
				Type.getMethodDescriptor(
						Type.getType(Number.class),
						Type.getType(Object.class),
						Type.getType(String.class)),
				false));

		return il;
	}

	public static AbstractInsnNode floatValueOf() {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Conversions.class),
				"floatValueOf",
				Type.getMethodDescriptor(
						Type.getType(Double.class),
						Type.getType(Object.class)),
				false);
	}

	public static AbstractInsnNode booleanValueOf() {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Conversions.class),
				"booleanValueOf",
				Type.getMethodDescriptor(
						Type.BOOLEAN_TYPE,
						Type.getType(Object.class)),
				false);
	}

	public static AbstractInsnNode unboxedNumberToLuaFormatString(Type tpe) {
		Check.isTrue(tpe.equals(Type.DOUBLE_TYPE) || tpe.equals(Type.LONG_TYPE));
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(LuaFormat.class),
				"toString",
				Type.getMethodDescriptor(
						Type.getType(String.class),
						tpe),
				false);
	}

	public static AbstractInsnNode boxedNumberToLuaFormatString() {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Conversions.class),
				"stringValueOf",
				Type.getMethodDescriptor(
						Type.getType(String.class),
						Type.getType(Number.class)),
				false);
	}

}

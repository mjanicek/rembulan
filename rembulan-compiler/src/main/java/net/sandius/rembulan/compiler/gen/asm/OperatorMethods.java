package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.RawOperators;
import net.sandius.rembulan.util.Check;
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
				"numberToString",
				Type.getMethodDescriptor(
						Type.getType(String.class),
						Type.getType(Number.class)),
				false);
	}
}

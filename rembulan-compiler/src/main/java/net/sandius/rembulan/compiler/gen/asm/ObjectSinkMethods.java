package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class ObjectSinkMethods {

	private ObjectSinkMethods() {
		// not to be instantiated
	}

	private static Type selfTpe() {
		return Type.getType(ObjectSink.class);
	}

	public static AbstractInsnNode size() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"size",
				Type.getMethodType(
						Type.INT_TYPE).getDescriptor(),
				false);
	}

	public static AbstractInsnNode get() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"get",
				Type.getMethodType(
						Type.getType(Object.class),
						Type.INT_TYPE).getDescriptor(),
				false);
	}

	public static InsnList get(int index) {
		Check.nonNegative(index);

		InsnList il = new InsnList();

		if (index <= 4) {
			String methodName = "_" + index;
			il.add(new MethodInsnNode(
					INVOKEVIRTUAL,
					selfTpe().getInternalName(),
					methodName,
					Type.getMethodType(
							Type.getType(Object.class)).getDescriptor(),
					false));
		}
		else {
			il.add(ASMUtils.loadInt(index));
			il.add(get());
		}

		return il;
	}

	public static AbstractInsnNode reset() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"reset",
				Type.getMethodType(
						Type.VOID_TYPE).getDescriptor(),
				false);
	}

	public static AbstractInsnNode push() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"push",
				Type.getMethodType(
						Type.VOID_TYPE,
						Type.getType(Object.class)).getDescriptor(),
				false);
	}

	public static AbstractInsnNode addAll() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"addAll",
				Type.getMethodType(
						Type.VOID_TYPE,
						ASMUtils.arrayTypeFor(Object.class)).getDescriptor(),
				false);
	}

	public static int adjustKind_setTo(int kind) {
		return kind > 0 ? (setTo_method(kind).exists() ? kind : 0) : 0;
	}

	public static int adjustKind_tailCall(int kind) {
		return kind > 0 ? (tailCall_method(kind).exists() ? kind : 0) : 0;
	}

	private static ReflectionUtils.Method setTo_method(int kind) {
		String methodName = kind > 0 ? "setTo" : "setToArray";
		return ReflectionUtils.virtualArgListMethodFromKind(ObjectSink.class, methodName, null, kind);
	}

	private static ReflectionUtils.Method tailCall_method(int kind) {
		String methodName = "tailCall";
		return ReflectionUtils.virtualArgListMethodFromKind(ObjectSink.class, methodName, new Class[] { Object.class }, kind);
	}

	public static AbstractInsnNode setTo(int kind) {
		return setTo_method(kind).toMethodInsnNode();
	}

	public static AbstractInsnNode tailCall(int kind) {
		return tailCall_method(kind).toMethodInsnNode();
	}

	public static AbstractInsnNode toArray() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"toArray",
				Type.getMethodType(
						ASMUtils.arrayTypeFor(Object.class)).getDescriptor(),
				false);
	}

	public static AbstractInsnNode drop() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"drop",
				Type.getMethodType(
						Type.VOID_TYPE,
						Type.INT_TYPE).getDescriptor(),
				false);
	}

	public static AbstractInsnNode prepend() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"prepend",
				Type.getMethodType(
						Type.VOID_TYPE,
						ASMUtils.arrayTypeFor(Object.class)).getDescriptor(),
				false);
	}

	public static AbstractInsnNode pushAll() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"pushAll",
				Type.getMethodType(
						Type.VOID_TYPE,
						ASMUtils.arrayTypeFor(Object.class)).getDescriptor(),
				false);
	}

	public static AbstractInsnNode setTailCallTarget() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"setTailCallTarget",
				Type.getMethodType(
						Type.VOID_TYPE,
						Type.getType(Object.class)).getDescriptor(),
				false);
	}

}

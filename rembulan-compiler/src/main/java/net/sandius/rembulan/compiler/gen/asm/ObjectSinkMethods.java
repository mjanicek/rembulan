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

	@Deprecated
	public static boolean canSaveNResults(int numValues) {
		// TODO: determine this by reading the ObjectSink interface?
		return numValues <= 5;
	}

	public static boolean canTailCallWithNArguments(int numValues) {
		// TODO: determine this by reading the ObjectSink interface?
		return numValues <= 5;
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

	public static AbstractInsnNode setTo(int numValues) {
		Check.nonNegative(numValues);
		if (numValues == 0) {
			return reset();
		}
		else {
			Check.isTrue(canSaveNResults(numValues));

			Type[] argTypes = new Type[numValues];
			Arrays.fill(argTypes, Type.getType(Object.class));

			return new MethodInsnNode(
					INVOKEVIRTUAL,
					selfTpe().getInternalName(),
					"setTo",
					Type.getMethodType(
							Type.VOID_TYPE,
							argTypes).getDescriptor(),
					false);
		}
	}

	public static AbstractInsnNode setToArray() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"setToArray",
				Type.getMethodType(
						Type.VOID_TYPE,
						ASMUtils.arrayTypeFor(Object.class)).getDescriptor(),
				false);
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

	public static AbstractInsnNode tailCall(int numCallArgs) {
		Check.isTrue(canTailCallWithNArguments(numCallArgs));

		Type[] callArgTypes = new Type[numCallArgs + 1];  // don't forget the call target
		Arrays.fill(callArgTypes, Type.getType(Object.class));

		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"tailCall",
				Type.getMethodType(
						Type.VOID_TYPE,
						callArgTypes).getDescriptor(),
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

package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;

public class ObjectSinkMethods {

	private ObjectSinkMethods() {
		// not to be instantiated
	}

	private static Type selfTpe() {
		return Type.getType(ObjectSink.class);
	}

	public static InsnList get(int index) {
		Check.nonNegative(index);

		InsnList il = new InsnList();

		if (index <= 4) {
			String methodName = "_" + index;
			il.add(new MethodInsnNode(
					INVOKEINTERFACE,
					selfTpe().getInternalName(),
					methodName,
					Type.getMethodType(
							Type.getType(Object.class)).getDescriptor(),
					true));
		}
		else {
			il.add(ASMUtils.loadInt(index));
			il.add(new MethodInsnNode(
					INVOKEINTERFACE,
					selfTpe().getInternalName(),
					"get",
					Type.getMethodType(
							Type.getType(Object.class),
							Type.INT_TYPE).getDescriptor(),
					true));
		}

		return il;
	}

	public static AbstractInsnNode reset() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				selfTpe().getInternalName(),
				"reset",
				Type.getMethodType(
						Type.VOID_TYPE).getDescriptor(),
				true);
	}

	public static AbstractInsnNode push() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				selfTpe().getInternalName(),
				"push",
				Type.getMethodType(
						Type.VOID_TYPE,
						Type.getType(Object.class)).getDescriptor(),
				true);
	}

	public static AbstractInsnNode addAll() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				selfTpe().getInternalName(),
				"addAll",
				Type.getMethodType(
						Type.VOID_TYPE,
						ASMUtils.arrayTypeFor(Object.class)).getDescriptor(),
				true);
	}

	@Deprecated
	public static boolean canSaveNResults(int numValues) {
		// TODO: determine this by reading the ObjectSink interface?
		return numValues <= 5;
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
					INVOKEINTERFACE,
					selfTpe().getInternalName(),
					"setTo",
					Type.getMethodType(
							Type.VOID_TYPE,
							argTypes).getDescriptor(),
					true);
		}
	}

	public static AbstractInsnNode setToArray() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				selfTpe().getInternalName(),
				"setToArray",
				Type.getMethodType(
						Type.VOID_TYPE,
						ASMUtils.arrayTypeFor(Object.class)).getDescriptor(),
				true);
	}

	public static AbstractInsnNode toArray() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				selfTpe().getInternalName(),
				"toArray",
				Type.getMethodType(
						ASMUtils.arrayTypeFor(Object.class)).getDescriptor(),
				true);
	}

	public static AbstractInsnNode drop() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				selfTpe().getInternalName(),
				"drop",
				Type.getMethodType(
						Type.VOID_TYPE,
						Type.INT_TYPE).getDescriptor(),
				true);
	}

	public static AbstractInsnNode prepend() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				selfTpe().getInternalName(),
				"prepend",
				Type.getMethodType(
						Type.VOID_TYPE,
						ASMUtils.arrayTypeFor(Object.class)).getDescriptor(),
				true);
	}

	public static AbstractInsnNode pushAll() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				selfTpe().getInternalName(),
				"pushAll",
				Type.getMethodType(
						Type.VOID_TYPE,
						ASMUtils.arrayTypeFor(Object.class)).getDescriptor(),
				true);
	}

	public static AbstractInsnNode tailCall(int numCallArgs) {
		Check.nonNegative(numCallArgs);

		// TODO: determine this by reading the ObjectSink interface?
		if (numCallArgs <= 4) {
			Type[] callArgTypes = new Type[numCallArgs + 1];  // don't forget the call target
			Arrays.fill(callArgTypes, Type.getType(Object.class));

			return new MethodInsnNode(
					INVOKEINTERFACE,
					selfTpe().getInternalName(),
					"tailCall",
					Type.getMethodType(
							Type.VOID_TYPE,
							callArgTypes).getDescriptor(),
					true);
		}
		else {
			// TODO: iterate and push
			throw new UnsupportedOperationException("Tail call with " + numCallArgs + " arguments");
		}
	}

	public static AbstractInsnNode markAsTailCall() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				selfTpe().getInternalName(),
				"markAsTailCall",
				Type.getMethodType(
						Type.VOID_TYPE).getDescriptor(),
				true);
	}

}

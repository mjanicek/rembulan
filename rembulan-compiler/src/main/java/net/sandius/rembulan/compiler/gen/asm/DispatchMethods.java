package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.Arrays;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class DispatchMethods {

	private DispatchMethods() {
		// not to be instantiated
	}

	public static AbstractInsnNode dynamic(String methodName, int numArgs) {
		ArrayList<Type> args = new ArrayList<>();
		args.add(Type.getType(LuaState.class));
		args.add(Type.getType(ObjectSink.class));
		for (int i = 0; i < numArgs; i++) {
			args.add(Type.getType(Object.class));
		}
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				methodName,
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						args.toArray(new Type[0])),
				false);
	}

	public static AbstractInsnNode numeric(String methodName, int numArgs) {
		Type[] args = new Type[numArgs];
		Arrays.fill(args, Type.getType(Number.class));
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				methodName,
				Type.getMethodDescriptor(
						Type.getType(Number.class),
						args),
				false);
	}

	public static AbstractInsnNode index() {
		return dynamic("index", 2);
	}

	public static AbstractInsnNode newindex() {
		return dynamic("newindex", 3);
	}

	public static AbstractInsnNode call(int kind) {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				"call",
				InvokeKind.staticMethodType(kind).getDescriptor(),
				false);
	}

	public static AbstractInsnNode continueLoop() {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				"continueLoop",
				Type.getMethodDescriptor(
						Type.BOOLEAN_TYPE,
						Type.getType(Number.class),
						Type.getType(Number.class),
						Type.getType(Number.class)),
				false);
	}

}

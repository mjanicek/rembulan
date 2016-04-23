package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Invokable;
import org.objectweb.asm.tree.AbstractInsnNode;

public class InvokableMethods {

	public static int adjustKind_invoke(int kind) {
		return kind > 0 ? (invoke_method(kind).exists() ? kind : 0) : 0;
	}

	public static ReflectionUtils.Method invoke_method(int kind) {
		return ReflectionUtils.virtualArgListMethodFromKind(
				Invokable.class, "invoke", new Class[] { ExecutionContext.class }, kind);
	}

	public static AbstractInsnNode invoke(int kind) {
		return invoke_method(kind).toMethodInsnNode();
	}

}

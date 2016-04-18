package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.Invokable;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import org.objectweb.asm.tree.AbstractInsnNode;

public class InvokableMethods {

	public static int adjustKind_invoke(int kind) {
		return kind > 0 ? (invoke_method(kind).exists() ? kind : 0) : 0;
	}

	public static ReflectionUtils.Method invoke_method(int kind) {
		return ReflectionUtils.virtualArgListMethodFromKind(
				Invokable.class, "invoke", new Class[] { LuaState.class, ObjectSink.class }, kind);
	}

	public static AbstractInsnNode invoke(int kind) {
		return invoke_method(kind).toMethodInsnNode();
	}

}

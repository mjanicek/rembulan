package net.sandius.rembulan.compiler.gen.asm.helpers;

import net.sandius.rembulan.core.ExecutionContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;

public abstract class ExecutionContextMethods {

	private ExecutionContextMethods() {
		// not to be instantiated or extended
	}

	private static Type selfTpe() {
		return Type.getType(ExecutionContext.class);
	}

	public static InsnList checkCpu(int cost) {
		InsnList il = new InsnList();

		il.add(ASMUtils.loadInt(cost));
		il.add(new MethodInsnNode(
				INVOKEINTERFACE,
				Type.getInternalName(ExecutionContext.class),
				"checkPreempt",
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						Type.INT_TYPE),
				true));

		return il;
	}

}

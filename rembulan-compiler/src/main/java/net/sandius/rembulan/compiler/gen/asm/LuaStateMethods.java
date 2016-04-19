package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Upvalue;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class LuaStateMethods {

	private LuaStateMethods() {
		// not to be instantiated
	}

	private static Type selfTpe() {
		return Type.getType(LuaState.class);
	}

	public static InsnList newTable(int array, int hash) {
		InsnList il = new InsnList();

		il.add(ASMUtils.loadInt(array));
		il.add(ASMUtils.loadInt(hash));

		il.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"newTable",
				Type.getMethodType(
						Type.getType(Table.class),
						Type.INT_TYPE,
						Type.INT_TYPE).getDescriptor(),
				false));

		return il;
	}

	public static AbstractInsnNode newUpvalue() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"newUpvalue",
				Type.getMethodType(
						Type.getType(Upvalue.class),
						Type.getType(Object.class)).getDescriptor(),
				false);
	}

	public static AbstractInsnNode checkCpu() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				selfTpe().getInternalName(),
				"checkCpu",
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						Type.INT_TYPE),
				false);
	}

}

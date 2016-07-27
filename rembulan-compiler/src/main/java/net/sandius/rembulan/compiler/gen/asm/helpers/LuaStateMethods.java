package net.sandius.rembulan.compiler.gen.asm.helpers;

import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.Table;
import org.objectweb.asm.Type;
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

}

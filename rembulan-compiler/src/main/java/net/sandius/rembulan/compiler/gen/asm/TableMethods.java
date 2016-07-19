package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.Table;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class TableMethods {

	private TableMethods() {
		// not to be instantiated
	}

	public static AbstractInsnNode rawset_int() {
		return new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(Table.class),
				"rawset",
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						Type.INT_TYPE,
						Type.getType(Object.class)),
				false);
	}

}

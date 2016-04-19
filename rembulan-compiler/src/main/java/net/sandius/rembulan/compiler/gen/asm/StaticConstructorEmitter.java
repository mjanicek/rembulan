package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.util.Check;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

public class StaticConstructorEmitter {

	private final ClassEmitter parent;

	private final MethodNode node;

	public StaticConstructorEmitter(ClassEmitter parent) {
		this.parent = Check.notNull(parent);

		this.node = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
	}

	public MethodNode node() {
		return node;
	}

	public void end() {
		emit();
	}

	public void emit() {
		InsnList il = node.instructions;

		LabelNode begin = new LabelNode();
		LabelNode end = new LabelNode();

		il.add(begin);

		if (!parent.hasUpvalues()) {
			il.add(new TypeInsnNode(NEW, parent.thisClassType().getInternalName()));
			il.add(new InsnNode(DUP));

			il.add(new MethodInsnNode(
					INVOKESPECIAL,
					parent.thisClassType().getInternalName(),
					"<init>",
					parent.constructor().methodType().getDescriptor(),
					false));

			il.add(new FieldInsnNode(
					PUTSTATIC,
					parent.thisClassType().getInternalName(),
					ClassEmitter.instanceFieldName(),
					parent.thisClassType().getDescriptor()));

			il.add(new InsnNode(RETURN));
		}

		il.add(end);
	}

}

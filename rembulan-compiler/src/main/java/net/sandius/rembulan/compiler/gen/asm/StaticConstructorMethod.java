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

class StaticConstructorMethod {

	private final ASMBytecodeEmitter context;
	private final ConstructorMethod ctorMethod;
	private final RunMethod runMethod;

	public StaticConstructorMethod(ASMBytecodeEmitter context, ConstructorMethod ctorMethod, RunMethod runMethod) {
		this.context = Check.notNull(context);
		this.ctorMethod = Check.notNull(ctorMethod);
		this.runMethod = Check.notNull(runMethod);
	}

	public boolean isEmpty() {
		return context.hasUpvalues() && runMethod.constFields().isEmpty();
	}

	public MethodNode methodNode() {

		MethodNode node = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);

		InsnList il = node.instructions;

		LabelNode begin = new LabelNode();
		LabelNode end = new LabelNode();

		il.add(begin);

		if (!context.hasUpvalues()) {
			il.add(new TypeInsnNode(NEW, context.thisClassType().getInternalName()));
			il.add(new InsnNode(DUP));

			il.add(new MethodInsnNode(
					INVOKESPECIAL,
					context.thisClassType().getInternalName(),
					"<init>",
					ctorMethod.methodType().getDescriptor(),
					false));

			il.add(new FieldInsnNode(
					PUTSTATIC,
					context.thisClassType().getInternalName(),
					context.instanceFieldName(),
					context.thisClassType().getDescriptor()));
		}

		if (!runMethod.constFields().isEmpty()) {
			for (RunMethod.ConstFieldInstance cfi : runMethod.constFields()) {
				il.add(cfi.instantiateInsns());
			}
		}

		il.add(new InsnNode(RETURN));
		il.add(end);

		return node;
	}

}

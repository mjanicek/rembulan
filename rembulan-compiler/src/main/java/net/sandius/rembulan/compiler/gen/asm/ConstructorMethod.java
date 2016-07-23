package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.compiler.ir.UpVar;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;

class ConstructorMethod {

	private final ASMBytecodeEmitter context;
	private final RunMethod runMethod;

	public ConstructorMethod(ASMBytecodeEmitter context, RunMethod runMethod) {
		this.context = Check.notNull(context);
		this.runMethod = Check.notNull(runMethod);
	}

	public Type methodType() {
		Type[] args = new Type[context.fn.upvals().size()];
		Arrays.fill(args, Type.getType(Upvalue.class));
		return Type.getMethodType(Type.VOID_TYPE, args);
	}

	public MethodNode methodNode() {

		MethodNode node = new MethodNode(
				ACC_PUBLIC,
				"<init>",
				methodType().getDescriptor(),
				null,
				null);


		InsnList il = node.instructions;

		LabelNode begin = new LabelNode();
		LabelNode end = new LabelNode();

		node.localVariables.add(new LocalVariableNode("this", context.thisClassType().getDescriptor(), null, begin, end, 0));

		il.add(begin);

		// superclass constructor
		il.add(new VarInsnNode(ALOAD, 0));
		il.add(new MethodInsnNode(
				INVOKESPECIAL,
				context.superClassType().getInternalName(),
				"<init>",
				Type.getMethodType(Type.VOID_TYPE).getDescriptor(),
				false));

		// initialise upvalue fields
		int idx = 0;
		for (UpVar uv : context.fn.upvals()) {
			String name = context.getUpvalueFieldName(uv);

			il.add(new VarInsnNode(ALOAD, 0));  // this
			il.add(new VarInsnNode(ALOAD, 1 + idx));  // upvalue #i
			il.add(new FieldInsnNode(PUTFIELD,
					context.thisClassType().getInternalName(),
					name,
					Type.getDescriptor(Upvalue.class)));

			node.localVariables.add(new LocalVariableNode(name, Type.getDescriptor(Upvalue.class), null, begin, end, idx));

			idx++;
		}

		// instantiate fields for closures that have no open upvalues
		for (RunMethod.ClosureFieldInstance cfi : runMethod.closureFields()) {
			context.fields().add(cfi.fieldNode());
			il.add(cfi.instantiateInsns());
		}

		il.add(new InsnNode(RETURN));

		il.add(end);

		node.maxStack = 2;
		node.maxLocals = context.fn.upvals().size() + 1;

		return node;
	}

}

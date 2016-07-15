package net.sandius.rembulan.compiler.gen.mk2;

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

	public ConstructorMethod(ASMBytecodeEmitter context) {
		this.context = Check.notNull(context);
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
		for (int i = 0; i < context.fn.upvals().size(); i++) {
			String name = context.getUpvalueFieldName(i);

			il.add(new VarInsnNode(ALOAD, 0));  // this
			il.add(new VarInsnNode(ALOAD, 1 + i));  // upvalue #i
			il.add(new FieldInsnNode(PUTFIELD,
					context.thisClassType().getInternalName(),
					name,
					Type.getDescriptor(Upvalue.class)));

			node.localVariables.add(new LocalVariableNode(name, Type.getDescriptor(Upvalue.class), null, begin, end, i));
		}

		// instantiate closures that have no open upvalues
		il.add(context.instantiateNestedInstanceFields());

		il.add(new InsnNode(RETURN));

		il.add(end);

		node.maxStack = 2;
		node.maxLocals = context.fn.upvals().size() + 1;

		return node;
	}

}

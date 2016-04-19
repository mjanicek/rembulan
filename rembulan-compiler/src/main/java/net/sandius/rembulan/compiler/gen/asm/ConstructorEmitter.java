package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;
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

public class ConstructorEmitter {

	private final ClassEmitter parent;

	private final MethodNode node;

	public ConstructorEmitter(ClassEmitter parent) {
		this.parent = Check.notNull(parent);

		this.node = new MethodNode(
				ACC_PUBLIC,
				"<init>",
				methodType().getDescriptor(),
				null,
				null);
	}

	public MethodNode node() {
		return node;
	}

	public Type methodType() {
		Type[] args = new Type[upvalues().size()];
		Arrays.fill(args, Type.getType(Upvalue.class));
		return Type.getMethodType(Type.VOID_TYPE, args);
	}

	protected ReadOnlyArray<Prototype.UpvalueDesc> upvalues() {
		return parent.context().prototype().getUpValueDescriptions();
	}

	public void end() {
		emit();
	}

	public void emit() {
		InsnList il = node.instructions;

		LabelNode begin = new LabelNode();
		LabelNode end = new LabelNode();

		node.localVariables.add(new LocalVariableNode("this", parent.thisClassType().getDescriptor(), null, begin, end, 0));

		il.add(begin);

		// superclass constructor
		il.add(new VarInsnNode(ALOAD, 0));
		il.add(new MethodInsnNode(
				INVOKESPECIAL,
				parent.superClassType().getInternalName(),
				"<init>",
				Type.getMethodType(Type.VOID_TYPE).getDescriptor(),
				false));

		for (int i = 0; i < upvalues().size(); i++) {
			String name = parent.getUpvalueFieldName(i);

			il.add(new VarInsnNode(ALOAD, 0));  // this
			il.add(new VarInsnNode(ALOAD, 1 + i));  // upvalue #i
			il.add(new FieldInsnNode(PUTFIELD,
					parent.thisClassType().getInternalName(),
					name,
					Type.getDescriptor(Upvalue.class)));

			node.localVariables.add(new LocalVariableNode(name, Type.getDescriptor(Upvalue.class), null, begin, end, i));
		}

		il.add(new InsnNode(RETURN));

		il.add(end);

		node.maxStack = 2;
		node.maxLocals = upvalues().size() + 1;
	}

}

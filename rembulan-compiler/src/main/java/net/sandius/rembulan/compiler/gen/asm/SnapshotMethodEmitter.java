package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.core.impl.DefaultSavedState;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;

public class SnapshotMethodEmitter {

	private final ClassEmitter parent;

	private final MethodNode node;

	public SnapshotMethodEmitter(ClassEmitter parent) {
		this.parent = Check.notNull(parent);

		this.node = new MethodNode(
				ACC_PRIVATE,
				methodName(),
				methodType().getDescriptor(),
				null,
				null);
	}

	public MethodNode node() {
		return node;
	}

	private String methodName() {
		return "snapshot";
	}

	private Type methodType() {
		ArrayList<Type> args = new ArrayList<>();

		args.add(Type.INT_TYPE);
		if (parent.isVararg()) {
			args.add(ASMUtils.arrayTypeFor(Object.class));
		}
		for (int i = 0; i < parent.runMethod().numOfRegisters(); i++) {
			args.add(Type.getType(Object.class));
		}
		return Type.getMethodType(Type.getType(Serializable.class), args.toArray(new Type[0]));
	}

	public MethodInsnNode methodInvokeInsn() {
		return new MethodInsnNode(
				INVOKESPECIAL,
				parent.thisClassType().getInternalName(),
				methodName(),
				methodType().getDescriptor(),
				false);
	}

	public void end() {
		emit();
	}

	public void emit() {
		InsnList il = node.instructions;
		LabelNode begin = new LabelNode();
		LabelNode end = new LabelNode();

		il.add(begin);

		il.add(new TypeInsnNode(NEW, Type.getInternalName(DefaultSavedState.class)));
		il.add(new InsnNode(DUP));

		int regOffset = parent.isVararg() ? 3 : 2;

		// resumption point
		il.add(new VarInsnNode(ILOAD, 1));

		// registers
		int numRegs = parent.runMethod().numOfRegisters();
		il.add(ASMUtils.loadInt(numRegs));
		il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
		for (int i = 0; i < numRegs; i++) {
			il.add(new InsnNode(DUP));
			il.add(ASMUtils.loadInt(i));
			il.add(new VarInsnNode(ALOAD, regOffset + i));
			il.add(new InsnNode(AASTORE));
		}

		// varargs
		if (parent.isVararg()) {
			il.add(new VarInsnNode(ALOAD, 2));
		}

		if (parent.isVararg()) {
			il.add(ASMUtils.ctor(
					Type.getType(DefaultSavedState.class),
					Type.INT_TYPE,
					ASMUtils.arrayTypeFor(Object.class),
					ASMUtils.arrayTypeFor(Object.class)));
		}
		else {
			il.add(ASMUtils.ctor(
					Type.getType(DefaultSavedState.class),
					Type.INT_TYPE,
					ASMUtils.arrayTypeFor(Object.class)));
		}

		il.add(new InsnNode(ARETURN));

		il.add(end);

		List<LocalVariableNode> locals = node.localVariables;

		locals.add(new LocalVariableNode("this", parent.thisClassType().getDescriptor(), null, begin, end, 0));
		locals.add(new LocalVariableNode("rp", Type.INT_TYPE.getDescriptor(), null, begin, end, 1));
		if (parent.isVararg()) {
			locals.add(new LocalVariableNode("varargs", ASMUtils.arrayTypeFor(Object.class).getDescriptor(), null, begin, end, 2));
		}
		for (int i = 0; i < parent.runMethod().numOfRegisters(); i++) {
			locals.add(new LocalVariableNode("r_" + i, Type.getDescriptor(Object.class), null, begin, end, regOffset + i));
		}

		node.maxLocals = 2 + parent.runMethod().numOfRegisters();
		node.maxStack = 4 + 3;  // 4 to get register array at top, +3 to add element to it
	}

}

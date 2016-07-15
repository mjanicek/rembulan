package net.sandius.rembulan.compiler.gen.mk2;

import net.sandius.rembulan.compiler.gen.asm.ASMUtils;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.impl.DefaultSavedState;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

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
import static org.objectweb.asm.Opcodes.RETURN;

class RunMethod {

	private final ASMBytecodeEmitter context;

	public RunMethod(ASMBytecodeEmitter context) {
		this.context = Check.notNull(context);
	}

	public int numOfRegisters() {
		return context.slots.numSlots();
	}

	public boolean isResumable() {
		// FIXME: not always
		return true;
	}

	public String[] throwsExceptions() {
		return new String[] { Type.getInternalName(ControlThrowable.class) };
	}

	public boolean usesSnapshotMethod() {
		return isResumable();
	}

	private String snapshotMethodName() {
		return "snapshot";
	}

	private Type snapshotMethodType() {
		ArrayList<Type> args = new ArrayList<>();

		args.add(Type.INT_TYPE);
		if (context.isVararg()) {
			args.add(ASMUtils.arrayTypeFor(Object.class));
		}
		for (int i = 0; i < numOfRegisters(); i++) {
			args.add(Type.getType(Object.class));
		}
		return Type.getMethodType(context.savedStateClassType(), args.toArray(new Type[0]));
	}

	public MethodInsnNode snapshotMethodInvokeInsn() {
		return new MethodInsnNode(
				INVOKESPECIAL,
				context.thisClassType().getInternalName(),
				snapshotMethodName(),
				snapshotMethodType().getDescriptor(),
				false);
	}

	public MethodNode snapshotMethodNode() {
		MethodNode node = new MethodNode(
				ACC_PRIVATE,
				snapshotMethodName(),
				snapshotMethodType().getDescriptor(),
				null,
				null);

		InsnList il = node.instructions;
		LabelNode begin = new LabelNode();
		LabelNode end = new LabelNode();

		il.add(begin);

		il.add(new TypeInsnNode(NEW, Type.getInternalName(DefaultSavedState.class)));
		il.add(new InsnNode(DUP));

		int regOffset = context.isVararg() ? 3 : 2;

		// resumption point
		il.add(new VarInsnNode(ILOAD, 1));

		// registers
		int numRegs = numOfRegisters();
		il.add(ASMUtils.loadInt(numRegs));
		il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
		for (int i = 0; i < numRegs; i++) {
			il.add(new InsnNode(DUP));
			il.add(ASMUtils.loadInt(i));
			il.add(new VarInsnNode(ALOAD, regOffset + i));
			il.add(new InsnNode(AASTORE));
		}

		// varargs
		if (context.isVararg()) {
			il.add(new VarInsnNode(ALOAD, 2));
		}

		if (context.isVararg()) {
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

		locals.add(new LocalVariableNode("this", context.thisClassType().getDescriptor(), null, begin, end, 0));
		locals.add(new LocalVariableNode("rp", Type.INT_TYPE.getDescriptor(), null, begin, end, 1));
		if (context.isVararg()) {
			locals.add(new LocalVariableNode("varargs", ASMUtils.arrayTypeFor(Object.class).getDescriptor(), null, begin, end, 2));
		}
		for (int i = 0; i < numOfRegisters(); i++) {
			locals.add(new LocalVariableNode("r_" + i, Type.getDescriptor(Object.class), null, begin, end, regOffset + i));
		}

		node.maxLocals = 2 + numOfRegisters();
		node.maxStack = 4 + 3;  // 4 to get register array at top, +3 to add element to it

		return node;
	}

	public String methodName() {
		return "run";
	}

	public Type methodType() {
		ArrayList<Type> args = new ArrayList<>();

		args.add(Type.getType(ExecutionContext.class));
		args.add(Type.INT_TYPE);
		if (context.isVararg()) {
			args.add(ASMUtils.arrayTypeFor(Object.class));
		}
		for (int i = 0; i < numOfRegisters(); i++) {
			args.add(Type.getType(Object.class));
		}
		return Type.getMethodType(Type.VOID_TYPE, args.toArray(new Type[0]));
	}

	public AbstractInsnNode methodInvokeInsn() {
		return new MethodInsnNode(
				INVOKESPECIAL,
				context.thisClassType().getInternalName(),
				methodName(),
				methodType().getDescriptor(),
				false);
	}

	public MethodNode methodNode() {
		MethodNode node = new MethodNode(
				ACC_PRIVATE,
				methodName(),
				methodType().getDescriptor(),
				null,
				throwsExceptions());

		// TODO
		node.instructions.add(new InsnNode(RETURN));

		return node;
	}

}

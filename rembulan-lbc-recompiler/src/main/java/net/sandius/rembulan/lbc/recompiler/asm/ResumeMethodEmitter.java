package net.sandius.rembulan.lbc.recompiler.asm;

import net.sandius.rembulan.compiler.gen.asm.ASMUtils;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.NonsuspendableFunctionException;
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

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class ResumeMethodEmitter {

	private final ClassEmitter parent;

	private final MethodNode node;

	public ResumeMethodEmitter(ClassEmitter parent) {
		this.parent = Check.notNull(parent);

		this.node = new MethodNode(
				ACC_PUBLIC,
				"resume",
				Type.getMethodType(
						Type.VOID_TYPE,
						Type.getType(ExecutionContext.class),
						ClassEmitter.savedStateType()).getDescriptor(),
						null,
				RunMethodEmitter.exceptions());
	}

	public MethodNode node() {
		return node;
	}

	public void end() {
		emit();
	}

	private void emit() {
		if (parent.runMethod().isResumable()) {
			InsnList il = node.instructions;
			List<LocalVariableNode> locals = node.localVariables;

			LabelNode begin = new LabelNode();
			LabelNode vars = new LabelNode();
			LabelNode end = new LabelNode();

			il.add(begin);

			il.add(new VarInsnNode(ALOAD, 2));
			il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(DefaultSavedState.class)));

			il.add(vars);

			il.add(new VarInsnNode(ASTORE, 3));

			il.add(new VarInsnNode(ALOAD, 0));  // this
			il.add(new VarInsnNode(ALOAD, 1));  // context

			il.add(new VarInsnNode(ALOAD, 3));  // saved state
			il.add(new MethodInsnNode(
					INVOKEVIRTUAL,
					Type.getInternalName(DefaultSavedState.class),
					"resumptionPoint",
					Type.getMethodDescriptor(
							Type.INT_TYPE),
					false
			));  // resumption point

			if (parent.isVararg()) {
				il.add(new VarInsnNode(ALOAD, 3));
				il.add(new MethodInsnNode(
						INVOKEVIRTUAL,
						Type.getInternalName(DefaultSavedState.class),
						"varargs",
						Type.getMethodDescriptor(
								ASMUtils.arrayTypeFor(Object.class)),
						false
				));
			}

			// registers
			if (parent.runMethod().numOfRegisters() > 0) {
				il.add(new VarInsnNode(ALOAD, 3));
				il.add(new MethodInsnNode(
						INVOKEVIRTUAL,
						Type.getInternalName(DefaultSavedState.class),
						"registers",
						Type.getMethodDescriptor(
								ASMUtils.arrayTypeFor(Object.class)),
						false
				));

				for (int i = 0; i < parent.runMethod().numOfRegisters(); i++) {

					// Note: it might be more elegant to use a local variable
					// to store the array instead of having to perform SWAPs

					if (i + 1 < parent.runMethod().numOfRegisters()) {
						il.add(new InsnNode(DUP));
					}
					il.add(ASMUtils.loadInt(i));
					il.add(new InsnNode(AALOAD));
					if (i + 1 < parent.runMethod().numOfRegisters()) {
						il.add(new InsnNode(SWAP));
					}
				}
			}

			// call run(...)
			il.add(parent.runMethod().methodInvokeInsn());

			il.add(new InsnNode(RETURN));
			il.add(end);

			locals.add(new LocalVariableNode("this", parent.thisClassType().getDescriptor(), null, begin, end, 0));
			locals.add(new LocalVariableNode("context", Type.getDescriptor(ExecutionContext.class), null, begin, end, 1));
			locals.add(new LocalVariableNode("suspendedState", ClassEmitter.savedStateType().getDescriptor(), null, begin, end, 2));
			locals.add(new LocalVariableNode("ss", Type.getDescriptor(DefaultSavedState.class), null, vars, end, 3));

			// TODO: maxStack, maxLocals
			node.maxStack = 3 + (parent.runMethod().numOfRegisters() > 0 ? 3: 0);
			node.maxLocals = 5;
		}
		else
		{
			InsnList il = node.instructions;
			List<LocalVariableNode> locals = node.localVariables;

			LabelNode begin = new LabelNode();
			LabelNode end = new LabelNode();

			il.add(begin);
			il.add(new TypeInsnNode(NEW, Type.getInternalName(NonsuspendableFunctionException.class)));
			il.add(new InsnNode(DUP));
			il.add(ASMUtils.ctor(NonsuspendableFunctionException.class));
			il.add(new InsnNode(ATHROW));
			il.add(end);

			locals.add(new LocalVariableNode("this", parent.thisClassType().getDescriptor(), null, begin, end, 0));
			locals.add(new LocalVariableNode("context", Type.getDescriptor(ExecutionContext.class), null, begin, end, 1));
			locals.add(new LocalVariableNode("suspendedState", ClassEmitter.savedStateType().getDescriptor(), null, begin, end, 2));

			node.maxStack = 2;
			node.maxLocals = 3;
		}
	}

}

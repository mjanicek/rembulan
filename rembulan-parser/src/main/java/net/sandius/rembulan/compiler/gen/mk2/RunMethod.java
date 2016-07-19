package net.sandius.rembulan.compiler.gen.mk2;

import net.sandius.rembulan.compiler.gen.asm.ASMUtils;
import net.sandius.rembulan.compiler.gen.asm.ClassEmitter;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.Resumable;
import net.sandius.rembulan.core.impl.DefaultSavedState;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

class RunMethod {

	public final int LV_CONTEXT = 1;
	public final int LV_RESUME = 2;
	public final int LV_VARARGS = 3;  // index of the varargs argument, if present

	private final ASMBytecodeEmitter context;

	public RunMethod(ASMBytecodeEmitter context) {
		this.context = Check.notNull(context);
	}

	public int numOfRegisters() {
		return context.slots.numSlots();
	}

	public int slotOffset() {
		return context.isVararg() ? LV_VARARGS + 1 : LV_VARARGS;
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

	private InsnList errorState(LabelNode label) {
		InsnList il = new InsnList();
		il.add(label);
		il.add(ASMUtils.frameSame());
		il.add(new TypeInsnNode(NEW, Type.getInternalName(IllegalStateException.class)));
		il.add(new InsnNode(DUP));
		il.add(ASMUtils.ctor(IllegalStateException.class));
		il.add(new InsnNode(ATHROW));
		return il;
	}

	private InsnList dispatchTable(LabelNode entryLabel, List<LabelNode> resumptionLabels, LabelNode errorStateLabel) {
		InsnList il = new InsnList();

		LabelNode[] labels = new LabelNode[resumptionLabels.size() + 1];
		labels[0] = entryLabel;
		for (int i = 0; i < resumptionLabels.size(); i++) {
			labels[1 + i] = resumptionLabels.get(i);
		}

		il.add(new VarInsnNode(ILOAD, LV_RESUME));
		il.add(new TableSwitchInsnNode(0, labels.length - 1, errorStateLabel, labels));
		return il;
	}

	private InsnList createSnapshot() {
		InsnList il = new InsnList();

		il.add(new VarInsnNode(ALOAD, 0));  // this
		il.add(new VarInsnNode(ALOAD, 0));
		il.add(new VarInsnNode(ILOAD, LV_RESUME));
		if (context.isVararg()) {
			il.add(new VarInsnNode(ALOAD, LV_VARARGS));
		}
		for (int i = 0; i < numOfRegisters(); i++) {
			il.add(new VarInsnNode(ALOAD, slotOffset() + i));
		}
		il.add(snapshotMethodInvokeInsn());

		return il;
	}

	protected InsnList resumptionHandler(LabelNode label) {
		InsnList il = new InsnList();

		il.add(label);
		il.add(ASMUtils.frameSame1(ControlThrowable.class));

		il.add(new InsnNode(DUP));

		il.add(createSnapshot());

		// register snapshot with the control exception
		il.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(ControlThrowable.class),
				"push",
				Type.getMethodType(
						Type.VOID_TYPE,
						Type.getType(Resumable.class),
						ClassEmitter.savedStateType()).getDescriptor(),
				false));

		// rethrow
		il.add(new InsnNode(ATHROW));

		return il;
	}

	public MethodNode methodNode() {
		MethodNode node = new MethodNode(
				ACC_PRIVATE,
				methodName(),
				methodType().getDescriptor(),
				null,
				throwsExceptions());

		InsnList insns = node.instructions;

		LabelNode l_begin = new LabelNode();
		LabelNode l_end = new LabelNode();

		BytecodeEmitVisitor visitor = new BytecodeEmitVisitor(context, this, context.slots, context.types);
		visitor.visit(context.fn.blocks());

		InsnList prefix = new InsnList();
		InsnList suffix = new InsnList();

		if (isResumable()) {
			LabelNode l_entry = new LabelNode();
			LabelNode l_error_state = new LabelNode();
			LabelNode l_handler_begin = new LabelNode();

			List<LabelNode> rls = visitor.resumptionLabels();

			assert (!rls.isEmpty());

			prefix.add(dispatchTable(l_entry, rls, l_error_state));
			prefix.add(l_entry);
			prefix.add(ASMUtils.frameSame());

			suffix.add(errorState(l_error_state));
			suffix.add(resumptionHandler(l_handler_begin));

			node.tryCatchBlocks.add(new TryCatchBlockNode(l_entry, l_error_state, l_handler_begin, Type.getInternalName(ControlThrowable.class)));
		}

		insns.add(l_begin);
		insns.add(prefix);
		insns.add(visitor.instructions());
		insns.add(suffix);
		insns.add(l_end);

		// local variables
		{
			List<LocalVariableNode> locals = node.localVariables;

			locals.add(new LocalVariableNode("this", context.thisClassType().getDescriptor(), null, l_begin, l_end, 0));
			locals.add(new LocalVariableNode("context", Type.getDescriptor(ExecutionContext.class), null, l_begin, l_end, LV_CONTEXT));
			locals.add(new LocalVariableNode("rp", Type.INT_TYPE.getDescriptor(), null, l_begin, l_end, LV_RESUME));

			if (context.isVararg()) {
				locals.add(new LocalVariableNode(
						"varargs",
						ASMUtils.arrayTypeFor(Object.class).getDescriptor(),
						null,
						l_begin,
						l_end,
						LV_VARARGS
						));
			}

			for (int i = 0; i < numOfRegisters(); i++) {
				locals.add(new LocalVariableNode("s_" + i, Type.getDescriptor(Object.class), null, l_begin, l_end, slotOffset() + i));
			}

			locals.addAll(visitor.locals());
		}

		return node;
	}

}

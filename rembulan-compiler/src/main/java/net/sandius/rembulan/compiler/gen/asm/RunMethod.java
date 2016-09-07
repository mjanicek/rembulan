/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.ExecutionContext;
import net.sandius.rembulan.Resumable;
import net.sandius.rembulan.compiler.gen.CodeSegmenter;
import net.sandius.rembulan.compiler.gen.SegmentedCode;
import net.sandius.rembulan.compiler.gen.asm.helpers.ASMUtils;
import net.sandius.rembulan.compiler.gen.asm.helpers.BoxedPrimitivesMethods;
import net.sandius.rembulan.compiler.ir.BasicBlock;
import net.sandius.rembulan.compiler.ir.Label;
import net.sandius.rembulan.impl.DefaultSavedState;
import net.sandius.rembulan.runtime.ResolvedControlThrowable;
import net.sandius.rembulan.runtime.UnresolvedControlThrowable;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

class RunMethod {

	public final int LV_CONTEXT = 1;
	public final int LV_RESUME = 2;
	public final int LV_VARARGS = 3;  // index of the varargs argument, if present

	public static final int ST_SHIFT_SEGMENT  = 24;
	public static final int ST_SHIFT_LABELIDX = 16;

	private final ASMBytecodeEmitter context;
	private final List<MethodNode> methodNodes;
	private final boolean resumable;

	private final List<ClosureFieldInstance> closureFields;
	private final List<ConstFieldInstance> constFields;

	interface LabelResolver {
		boolean isLocalLabel(Label l);
		int labelStateIndex(Label l);
	}

	static int labelStateIdx(SegmentedCode.LabelEntry le) {
		return (le.segmentIdx << ST_SHIFT_SEGMENT) | (le.idx << ST_SHIFT_LABELIDX);
	}

	public RunMethod(ASMBytecodeEmitter context) {
		this.context = Check.notNull(context);

		final SegmentedCode segmentedCode = CodeSegmenter.segment(
				context.fn.code(),
				context.compilerSettings.nodeSizeLimit());

		this.methodNodes = new ArrayList<>();

		this.closureFields = new ArrayList<>();
		this.constFields = new ArrayList<>();

		if (segmentedCode.isSingleton()) {
			// as before
			BytecodeEmitVisitor visitor = new BytecodeEmitVisitor(
					context, this, context.slots, context.types, closureFields, constFields, -1,
					new LabelResolver() {
						@Override
						public boolean isLocalLabel(Label l) {
							return true;
						}

						@Override
						public int labelStateIndex(Label l) {
							throw new IllegalStateException();
						}
					});

			this.methodNodes.add(emitSingletonRunMethod(visitor, segmentedCode.segments().get(0)));
			this.resumable = visitor.isResumable();
		}
		else {
			// split up into multiple segments

			boolean resumable = false;
			for (int i = 0; i < segmentedCode.segments().size(); i++) {

				final int thisSegmentIdx = i;

				BytecodeEmitVisitor visitor = new BytecodeEmitVisitor(
						context, this, context.slots, context.types, closureFields, constFields, i,
						new LabelResolver() {
							@Override
							public boolean isLocalLabel(Label l) {
								return segmentedCode.labelEntry(l).segmentIdx == thisSegmentIdx;
							}

							@Override
							public int labelStateIndex(Label l) {
								return labelStateIdx(segmentedCode.labelEntry(l));
							}
						});

				this.methodNodes.add(emitSegmentedSubRunMethod(i, visitor, segmentedCode.segments().get(i)));
				resumable |= visitor.isResumable();
			}

			this.resumable = resumable;

			this.methodNodes.add(emitSegmentedRunMethod(segmentedCode.segments().size()));

//			throw new UnsupportedOperationException();  // TODO
		}
	}

	public int numOfRegisters() {
		return context.slots.numSlots();
	}

	public int slotOffset() {
		return context.isVararg() ? LV_VARARGS + 1 : LV_VARARGS;
	}

	public boolean isResumable() {
		return resumable;
	}

	public String[] throwsExceptions() {
		return new String[] { Type.getInternalName(ResolvedControlThrowable.class) };
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

		// resumption point
		il.add(new VarInsnNode(ILOAD, 1));

		// registers
		int numRegs = numOfRegisters() + (context.isVararg() ? 1 : 0);
		int regOffset = context.isVararg() ? 3 : 2;

		il.add(ASMUtils.loadInt(numRegs));
		il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
		{
			for (int i = 0; i < numRegs; i++) {
				il.add(new InsnNode(DUP));
				il.add(ASMUtils.loadInt(i));
				il.add(new VarInsnNode(ALOAD, 2 + i));
				il.add(new InsnNode(AASTORE));
			}
		}

		il.add(ASMUtils.ctor(
				Type.getType(DefaultSavedState.class),
				Type.INT_TYPE,
				ASMUtils.arrayTypeFor(Object.class)));

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

	private Type methodType(Type returnType) {
		ArrayList<Type> args = new ArrayList<>();

		args.add(Type.getType(ExecutionContext.class));
		args.add(Type.INT_TYPE);
		if (context.isVararg()) {
			args.add(ASMUtils.arrayTypeFor(Object.class));
		}
		for (int i = 0; i < numOfRegisters(); i++) {
			args.add(Type.getType(Object.class));
		}
		return Type.getMethodType(returnType, args.toArray(new Type[0]));
	}

	public Type methodType() {
		return methodType(Type.VOID_TYPE);
	}

	private Type subMethodType() {
		return methodType(context.savedStateClassType());
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

	private InsnList dispatchTable(List<LabelNode> extLabels, List<LabelNode> resumptionLabels, LabelNode errorStateLabel) {
		InsnList il = new InsnList();

		assert (!extLabels.isEmpty());

		ArrayList<LabelNode> labels = new ArrayList<>();
		labels.addAll(extLabels);
		labels.addAll(resumptionLabels);
		LabelNode[] labelArray = labels.toArray(new LabelNode[labels.size()]);

		int min = 1 - extLabels.size();
		int max = resumptionLabels.size();

		il.add(new VarInsnNode(ILOAD, LV_RESUME));
		il.add(new TableSwitchInsnNode(min, max, errorStateLabel, labelArray));
		return il;
	}

	InsnList createSnapshot() {
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
		il.add(ASMUtils.frameSame1(UnresolvedControlThrowable.class));

		il.add(createSnapshot());

		// register snapshot with the control exception
		il.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(UnresolvedControlThrowable.class),
				"push",
				Type.getMethodType(
						Type.getType(ResolvedControlThrowable.class),
						Type.getType(Resumable.class),
						Type.getType(Object.class)).getDescriptor(),
				false));

		// rethrow
		il.add(new InsnNode(ATHROW));

		return il;
	}

	static class ClosureFieldInstance {

		private final FieldNode fieldNode;
		private final InsnList instantiateInsns;

		public ClosureFieldInstance(FieldNode fieldNode, InsnList instantiateInsns) {
			this.fieldNode = Check.notNull(fieldNode);
			this.instantiateInsns = Check.notNull(instantiateInsns);
		}

		public FieldNode fieldNode() {
			return fieldNode;
		}

		public InsnList instantiateInsns() {
			return instantiateInsns;
		}

	}

	public List<ClosureFieldInstance> closureFields() {
		return closureFields;
	}

	static class ConstFieldInstance {

		private final Object value;
		private final String fieldName;
		private final Type ownerClassType;
		private final Type fieldType;

		public ConstFieldInstance(Object value, String fieldName, Type ownerClassType, Type fieldType) {
			this.value = Check.notNull(value);
			this.fieldName = Check.notNull(fieldName);
			this.ownerClassType = Check.notNull(ownerClassType);
			this.fieldType = Check.notNull(fieldType);
		}

		public Object value() {
			return value;
		}

		public FieldNode fieldNode() {
			return new FieldNode(
					ACC_PRIVATE + ACC_STATIC + ACC_FINAL,
					fieldName,
					fieldType.getDescriptor(),
					null,
					null);
		}

		public InsnList instantiateInsns() {
			InsnList il = new InsnList();
			il.add(BoxedPrimitivesMethods.loadBoxedConstant(value));
			il.add(new FieldInsnNode(
					PUTSTATIC,
					ownerClassType.getInternalName(),
					fieldName,
					fieldType.getDescriptor()));
	        return il;
		}

		public InsnList accessInsns() {
			InsnList il = new InsnList();
			il.add(new FieldInsnNode(
					GETSTATIC,
					ownerClassType.getInternalName(),
					fieldName,
					fieldType.getDescriptor()));
			return il;
		}

	}

	public List<ConstFieldInstance> constFields() {
		return constFields;
	}

	private List<LocalVariableNode> baseLocals(LabelNode l_begin, LabelNode l_end) {
		List<LocalVariableNode> locals = new ArrayList<>();

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

		return locals;
	}

	private void addLocals(MethodNode node, LabelNode l_begin, LabelNode l_end, BytecodeEmitVisitor visitor) {
		List<LocalVariableNode> locals = node.localVariables;
		locals.addAll(baseLocals(l_begin, l_end));
		locals.addAll(visitor.locals());
	}

	private MethodNode emitRunMethod(String methodName, Type returnType, BytecodeEmitVisitor visitor, List<BasicBlock> blocks, boolean sub) {
		MethodNode node = new MethodNode(
				ACC_PRIVATE,
				methodName,
				methodType(returnType).getDescriptor(),
				null,
				throwsExceptions());

		InsnList insns = node.instructions;

		LabelNode l_begin = new LabelNode();
		LabelNode l_end = new LabelNode();

		visitor.visitBlocks(blocks);

		InsnList prefix = new InsnList();
		InsnList suffix = new InsnList();

		final LabelNode l_head;
		final List<LabelNode> els = new ArrayList<>();
		if (sub) {
			assert (!blocks.isEmpty());
			for (int i = blocks.size() - 1; i >= 0; i--) {
				BasicBlock blk = blocks.get(i);
				LabelNode l = visitor.labels.get(blk.label());
				assert (l != null);
				els.add(l);
			}
			l_head = visitor.labels.get(blocks.get(0).label());
		}
		else {
			l_head = new LabelNode();
			els.add(l_head);
		}

		assert (l_head != null);

		if (visitor.isResumable()) {
			LabelNode l_error_state = new LabelNode();
			LabelNode l_handler_begin = new LabelNode();

			List<LabelNode> rls = visitor.resumptionLabels();

			assert (!rls.isEmpty() || !els.isEmpty());

			prefix.add(dispatchTable(els, rls, l_error_state));

			final LabelNode l_entry = l_head;

			if (!sub) {
				prefix.add(l_entry);
				prefix.add(ASMUtils.frameSame());
			}

			suffix.add(errorState(l_error_state));
			suffix.add(resumptionHandler(l_handler_begin));

			node.tryCatchBlocks.add(new TryCatchBlockNode(l_entry, l_error_state, l_handler_begin, Type.getInternalName(UnresolvedControlThrowable.class)));
		}

		insns.add(l_begin);
		insns.add(prefix);
		insns.add(visitor.instructions());
		insns.add(suffix);
		insns.add(l_end);

		addLocals(node, l_begin, l_end, visitor);

		return node;
	}

	private MethodNode emitSingletonRunMethod(BytecodeEmitVisitor visitor, List<BasicBlock> blocks) {
		return emitRunMethod(methodName(), Type.VOID_TYPE, visitor, blocks, false);
	}

	private String subRunMethodName(int segmentIdx) {
		return "run_" + segmentIdx;
	}

	private MethodNode emitSegmentedSubRunMethod(int segmentIdx, BytecodeEmitVisitor visitor, List<BasicBlock> blocks) {
		return emitRunMethod(subRunMethodName(segmentIdx), context.savedStateClassType(), visitor, blocks, true);
	}

	private MethodNode emitSegmentedRunMethod(int numOfSegments) {
		MethodNode node = new MethodNode(
				ACC_PRIVATE,
				methodName(),
				methodType().getDescriptor(),
				null,
				throwsExceptions());

		InsnList il = node.instructions;

		int lvOffset = slotOffset() + numOfRegisters();

		int lv_rpp        = lvOffset + 0;
		int lv_methodIdx  = lvOffset + 1;
		int lv_jmpIdx     = lvOffset + 2;
		int lv_stateIdx   = lvOffset + 3;
		int lv_savedState = lvOffset + 4;

		LabelNode l_top = new LabelNode();
		LabelNode l_ret = new LabelNode();
		LabelNode l_end = new LabelNode();

		LabelNode l_rpp = new LabelNode();
		LabelNode l_methodIdx = new LabelNode();
		LabelNode l_jmpIdx = new LabelNode();
		LabelNode l_stateIdx = new LabelNode();
		LabelNode l_savedState = new LabelNode();

		il.add(l_top);
		il.add(new FrameNode(F_SAME, 0, null, 0, null));

		// rpp = rp & ((1 << ST_SHIFT_SEGMENT) - 1)
		il.add(new VarInsnNode(ILOAD, LV_RESUME));
		il.add(ASMUtils.loadInt((1 << ST_SHIFT_SEGMENT) - 1));
		il.add(new InsnNode(IAND));
		il.add(new VarInsnNode(ISTORE, lv_rpp));
		il.add(l_rpp);
		il.add(new FrameNode(F_APPEND, 1, new Object[] { Opcodes.INTEGER }, 0, null));

		// methodIdx = rp >>> ST_SHIFT_SEGMENT
		il.add(new VarInsnNode(ILOAD, LV_RESUME));
		il.add(ASMUtils.loadInt(ST_SHIFT_SEGMENT));
		il.add(new InsnNode(IUSHR));
		il.add(new VarInsnNode(ISTORE, lv_methodIdx));
		il.add(l_methodIdx);
		il.add(new FrameNode(F_APPEND, 1, new Object[] { Opcodes.INTEGER }, 0, null));

		// jmpIdx = rpp >>> ST_SHIFT_LABELIDX
		il.add(new VarInsnNode(ILOAD, lv_rpp));
		il.add(ASMUtils.loadInt(ST_SHIFT_LABELIDX));
		il.add(new InsnNode(IUSHR));
		il.add(new VarInsnNode(ISTORE, lv_jmpIdx));
		il.add(l_jmpIdx);
		il.add(new FrameNode(F_APPEND, 1, new Object[] { Opcodes.INTEGER }, 0, null));

		// stateIdx = (rp & ((1 << ST_SHIFT_LABELIDX) - 1)) - jmpIdx
		il.add(new VarInsnNode(ILOAD, LV_RESUME));
		il.add(ASMUtils.loadInt((1 << ST_SHIFT_LABELIDX) - 1));
		il.add(new InsnNode(IAND));
		il.add(new VarInsnNode(ILOAD, lv_jmpIdx));
		il.add(new InsnNode(ISUB));
		il.add(new VarInsnNode(ISTORE, lv_stateIdx));
		il.add(l_stateIdx);
		il.add(new FrameNode(F_APPEND, 1, new Object[] { Opcodes.INTEGER }, 0, null));

		// savedState = null
		il.add(new InsnNode(ACONST_NULL));
		il.add(new VarInsnNode(ASTORE, lv_savedState));
		il.add(l_savedState);
		il.add(new FrameNode(F_APPEND, 1, new Object[] { context.savedStateClassType().getInternalName() }, 0, null));

		// switch on methodIdx

		LabelNode l_after = new LabelNode();

		LabelNode l_error = new LabelNode();
		LabelNode[] l_invokes = new LabelNode[numOfSegments];
		for (int i = 0; i < numOfSegments; i++) {
			l_invokes[i] = new LabelNode();
		}

		il.add(new VarInsnNode(ILOAD, lv_methodIdx));
		il.add(new TableSwitchInsnNode(0, numOfSegments - 1, l_error, l_invokes));

		for (int i = 0; i < numOfSegments; i++) {
			il.add(l_invokes[i]);
			il.add(new FrameNode(F_SAME, 0, null, 0, null));
			// push arguments to stack
			il.add(new VarInsnNode(ALOAD, 0));
			il.add(new VarInsnNode(ALOAD, LV_CONTEXT));
			il.add(new VarInsnNode(ILOAD, lv_stateIdx));  // pass stateIdx to the sub-method
			if (context.isVararg()) {
				il.add(new VarInsnNode(ALOAD, LV_VARARGS));
			}
			for (int j = 0; j < numOfRegisters(); j++) {
				il.add(new VarInsnNode(ALOAD, slotOffset() + j));
			}

			il.add(new MethodInsnNode(INVOKESPECIAL,
					context.thisClassType().getInternalName(),
					subRunMethodName(i),
					subMethodType().getDescriptor(),
					false));

			il.add(new VarInsnNode(ASTORE, lv_savedState));
			il.add(new JumpInsnNode(GOTO, l_after));
		}

		// error state
		il.add(errorState(l_error));

		il.add(l_after);
		il.add(new FrameNode(F_SAME, 0, null, 0, null));

		il.add(new VarInsnNode(ALOAD, lv_savedState));
		il.add(new JumpInsnNode(IFNULL, l_ret));  // savedState == null ?

		// continuing: savedState != null

		// FIXME: taken from ResumeMethod -- beware of code duplication!

		il.add(new VarInsnNode(ALOAD, lv_savedState));  // saved state
		il.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				Type.getInternalName(DefaultSavedState.class),
				"resumptionPoint",
				Type.getMethodDescriptor(
						Type.INT_TYPE),
				false
		));  // resumption point
		il.add(new VarInsnNode(ISTORE, LV_RESUME));

		// registers
		if (context.isVararg() || numOfRegisters() > 0) {
			il.add(new VarInsnNode(ALOAD, lv_savedState));
			il.add(new MethodInsnNode(
					INVOKEVIRTUAL,
					Type.getInternalName(DefaultSavedState.class),
					"registers",
					Type.getMethodDescriptor(
							ASMUtils.arrayTypeFor(Object.class)),
					false
			));

			int numRegs = numOfRegisters() + (context.isVararg() ? 1 : 0);

			for (int i = 0; i < numRegs; i++) {
				if (i + 1 < numRegs) {
					il.add(new InsnNode(DUP));
				}
				il.add(ASMUtils.loadInt(i));
				il.add(new InsnNode(AALOAD));
				if (i == 0 && context.isVararg()) {
					il.add(new TypeInsnNode(CHECKCAST, ASMUtils.arrayTypeFor(Object.class).getInternalName()));
				}
				il.add(new VarInsnNode(ASTORE, LV_VARARGS + i));
			}
		}

		// loop back to the beginning
		il.add(new JumpInsnNode(GOTO, l_top));

		// got a null, that's the end
		il.add(l_ret);
		il.add(new FrameNode(F_SAME, 0, null, 0, null));
		il.add(new InsnNode(RETURN));

		il.add(l_end);

		// add local variables
		node.localVariables.addAll(baseLocals(l_top, l_end));
		node.localVariables.add(new LocalVariableNode("rpp", Type.INT_TYPE.getDescriptor(), null, l_rpp, l_ret, lv_rpp));
		node.localVariables.add(new LocalVariableNode("methodIdx", Type.INT_TYPE.getDescriptor(), null, l_methodIdx, l_ret, lv_methodIdx));
		node.localVariables.add(new LocalVariableNode("jmpIdx", Type.INT_TYPE.getDescriptor(), null, l_jmpIdx, l_ret, lv_jmpIdx));
		node.localVariables.add(new LocalVariableNode("stateIdx", Type.INT_TYPE.getDescriptor(), null, l_stateIdx, l_ret, lv_stateIdx));
		node.localVariables.add(new LocalVariableNode("savedState", context.savedStateClassType().getDescriptor(), null, l_savedState, l_ret, lv_savedState));


		return node;
	}

	public List<MethodNode> methodNodes() {
		return methodNodes;
	}

}

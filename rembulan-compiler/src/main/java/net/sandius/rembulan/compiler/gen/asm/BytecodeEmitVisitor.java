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
import net.sandius.rembulan.LuaState;
import net.sandius.rembulan.ReturnBuffer;
import net.sandius.rembulan.Table;
import net.sandius.rembulan.Variable;
import net.sandius.rembulan.compiler.CompilerSettings;
import net.sandius.rembulan.compiler.FunctionId;
import net.sandius.rembulan.compiler.analysis.SlotAllocInfo;
import net.sandius.rembulan.compiler.analysis.TypeInfo;
import net.sandius.rembulan.compiler.gen.ClassNameTranslator;
import net.sandius.rembulan.compiler.gen.asm.helpers.ASMUtils;
import net.sandius.rembulan.compiler.gen.asm.helpers.BoxedPrimitivesMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.ConversionMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.DispatchMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.ExecutionContextMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.LuaStateMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.ReturnBufferMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.TableMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.VariableMethods;
import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static net.sandius.rembulan.compiler.gen.asm.helpers.DispatchMethods.*;
import static org.objectweb.asm.Opcodes.*;

class BytecodeEmitVisitor extends CodeVisitor {

	private final ASMBytecodeEmitter context;
	private final RunMethod runMethod;

	private final SlotAllocInfo slots;
	private final TypeInfo types;

	private final int segmentIdx;
	private final RunMethod.LabelResolver resolver;

	final Map<Object, LabelNode> labels;
	private final ArrayList<LabelNode> resumptionPoints;

	private final InsnList il;
	private final List<LocalVariableNode> locals;

	private final List<RunMethod.ClosureFieldInstance> instanceLevelClosures;

	private final List<RunMethod.ConstFieldInstance> constFields;

	public BytecodeEmitVisitor(
			ASMBytecodeEmitter context,
			RunMethod runMethod,
			SlotAllocInfo slots,
			TypeInfo types,
			List<RunMethod.ClosureFieldInstance> instanceLevelClosures,
			List<RunMethod.ConstFieldInstance> constFields,
			int segmentIdx,
			RunMethod.LabelResolver resolver) {

		this.context = Check.notNull(context);
		this.runMethod = Check.notNull(runMethod);
		this.slots = Check.notNull(slots);
		this.types = Check.notNull(types);

		this.segmentIdx = segmentIdx;
		this.resolver = Check.notNull(resolver);

		this.labels = new HashMap<>();
		this.resumptionPoints = new ArrayList<>();

		this.il = new InsnList();
		this.locals = new ArrayList<>();

		this.instanceLevelClosures = Check.notNull(instanceLevelClosures);
		this.constFields = Check.notNull(constFields);
	}

	private boolean isSub() {
		return segmentIdx >= 0;
	}

	public InsnList instructions() {
		return il;
	}

	public List<LocalVariableNode> locals() {
		return locals;
	}

	public List<RunMethod.ClosureFieldInstance> instanceLevelClosures() {
		return instanceLevelClosures;
	}

	public List<RunMethod.ConstFieldInstance> constFields() {
		return constFields;
	}

	protected int slot(AbstractVal v) {
		return runMethod.slotOffset() + slots.slotOf(v);
	}

	protected int slot(Var v) {
		return runMethod.slotOffset() + slots.slotOf(v);
	}

	protected int nextLocalVariableIndex() {
		return runMethod.slotOffset() + slots.numSlots();
	}

	private LabelNode l(Object o) {
		LabelNode l = labels.get(o);

		if (l != null) {
			return l;
		}
		else {
			LabelNode nl = new LabelNode();
			labels.put(o, nl);
			return nl;
		}
	}

	private RunMethod.ConstFieldInstance newConstFieldInstance(Object constValue, int idx) {
		Check.notNull(constValue);

		String fieldName = "_k_" + idx;

		final Type t;
		if (constValue instanceof Double) {
			t = Type.getType(Double.class);
		}
		else if (constValue instanceof Long) {
			t = Type.getType(Long.class);
		}
		else {
			throw new UnsupportedOperationException("Illegal constant: " + constValue);
		}

		return new RunMethod.ConstFieldInstance(constValue, fieldName, context.thisClassType(), t);
	}

	private InsnList loadCachedConst(Object constValue) {
		for (RunMethod.ConstFieldInstance cfi : constFields) {
			if (cfi.value().equals(constValue)) {
				return cfi.accessInsns();
			}
		}

		RunMethod.ConstFieldInstance cfi = newConstFieldInstance(constValue, constFields.size());
		constFields.add(cfi);
		return cfi.accessInsns();
	}

	public AbstractInsnNode loadExecutionContext() {
		return new VarInsnNode(ALOAD, runMethod.LV_CONTEXT);
	}

	static AbstractInsnNode loadState() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				Type.getInternalName(ExecutionContext.class),
				"getState",
				Type.getMethodDescriptor(
						Type.getType(LuaState.class)),
				true);
	}

	static AbstractInsnNode loadReturnBuffer() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				Type.getInternalName(ExecutionContext.class),
				"getReturnBuffer",
				Type.getMethodDescriptor(
						Type.getType(ReturnBuffer.class)),
				true);
	}

	public InsnList retrieve_0() {
		InsnList il = new InsnList();

		il.add(loadExecutionContext());
		il.add(loadReturnBuffer());
		il.add(ReturnBufferMethods.get(0));

		return il;
	}

	public InsnList loadUpvalueRef(UpVar uv) {
		InsnList il = new InsnList();

		il.add(new VarInsnNode(ALOAD, 0));
		il.add(new FieldInsnNode(
				GETFIELD,
				context.thisClassType().getInternalName(),
				context.getUpvalueFieldName(uv),
				Type.getDescriptor(Variable.class)));

		return il;
	}

	private InsnList saveState(int state) {
		InsnList il = new InsnList();
		il.add(ASMUtils.loadInt(state));
		il.add(new VarInsnNode(ISTORE, runMethod.LV_RESUME));
		return il;
	}

	public void visitBlocks(List<BasicBlock> blocks) {
		Iterator<BasicBlock> it = blocks.iterator();
		while (it.hasNext()) {
			BasicBlock b = it.next();
			visit(b);
		}
	}

	class ResumptionPoint {

		public final int index;

		private ResumptionPoint(int index) {
			this.index = index;
		}

		public LabelNode label() {
			return l(this);
		}

		public InsnList save() {
			int st = !isSub()
					? index + 1
					: segmentIdx << RunMethod.ST_SHIFT_SEGMENT | (index + 1);
			return saveState(st);
		}

		public InsnList resume() {
			InsnList il = new InsnList();

			il.add(label());
			il.add(ASMUtils.frameSame());

			return il;
		}
	}

	protected ResumptionPoint newResumptionPoint() {
		int idx = resumptionPoints.size();
		ResumptionPoint rp = new ResumptionPoint(idx);
		resumptionPoints.add(rp.label());
		return rp;
	}

	public boolean isResumable() {
		return isSub() || resumptionPoints.size() > 0;
	}

	public List<LabelNode> resumptionLabels() {
		return resumptionPoints;
	}

	private InsnList _return() {
		InsnList il = new InsnList();
		if (!isSub()) {
			il.add(new InsnNode(RETURN));
		}
		else {
			il.add(new InsnNode(ACONST_NULL));
			il.add(new InsnNode(ARETURN));
		}
		return il;
	}

	private InsnList _nonLocalGoto(Label label) {
		InsnList il = new InsnList();
		int st = resolver.labelStateIndex(label);
		il.add(saveState(st));
		il.add(runMethod.createSnapshot());
		il.add(new InsnNode(ARETURN));
		return il;
	}

	private InsnList _goto(Label label) {
		InsnList il = new InsnList();
		if (!isSub() || resolver.isLocalLabel(label)) {
			il.add(new JumpInsnNode(GOTO, l(label)));
		}
		else {
			il.add(_nonLocalGoto(label));
		}
		return il;
	}

	private InsnList _next(Label label) {
		InsnList il = new InsnList();
		if (!isSub() || resolver.isLocalLabel(label)) {
			// no-op
		}
		else {
			il.add(_nonLocalGoto(label));
		}
		return il;
	}

	@Override
	public void visit(PhiStore node) {
		il.add(new VarInsnNode(ALOAD, slot(node.src())));
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(PhiLoad node) {
		il.add(new VarInsnNode(ALOAD, slot(node.src())));
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(VarInit node) {
		if (types.isReified(node.var())) {
			il.add(new TypeInsnNode(NEW, Type.getInternalName(Variable.class)));
			il.add(new InsnNode(DUP));
			il.add(new VarInsnNode(ALOAD, slot(node.src())));
			il.add(VariableMethods.constructor());
			il.add(new VarInsnNode(ASTORE, slot(node.var())));
		}
		else {
			il.add(new VarInsnNode(ALOAD, slot(node.src())));
			il.add(new VarInsnNode(ASTORE, slot(node.var())));
		}
	}

	@Override
	public void visit(VarStore node) {
		if (types.isReified(node.var())) {
			il.add(new VarInsnNode(ALOAD, slot(node.var())));
			il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Variable.class)));
			il.add(new VarInsnNode(ALOAD, slot(node.src())));
			il.add(VariableMethods.set());
		}
		else {
			il.add(new VarInsnNode(ALOAD, slot(node.src())));
			il.add(new VarInsnNode(ASTORE, slot(node.var())));
		}
	}

	@Override
	public void visit(VarLoad node) {
		if (types.isReified(node.var())) {
			il.add(new VarInsnNode(ALOAD, slot(node.var())));
			il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Variable.class)));
			il.add(VariableMethods.get());
		}
		else {
			il.add(new VarInsnNode(ALOAD, slot(node.var())));
		}
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(UpLoad node) {
		il.add(loadUpvalueRef(node.upval()));
		il.add(VariableMethods.get());
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(UpStore node) {
		il.add(loadUpvalueRef(node.upval()));
		il.add(new VarInsnNode(ALOAD, slot(node.src())));
		il.add(VariableMethods.set());
	}

	@Override
	public void visit(LoadConst.Nil node) {
		il.add(new InsnNode(ACONST_NULL));
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(LoadConst.Bool node) {
		il.add(BoxedPrimitivesMethods.loadBoxedBoolean(node.value()));
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(LoadConst.Int node) {
		if (context.compilerSettings.constCaching()) {
			il.add(loadCachedConst(node.value()));
		}
		else {
			il.add(ASMUtils.loadLong(node.value()));
			il.add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, Type.getType(Long.class)));
		}
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(LoadConst.Flt node) {
		if (context.compilerSettings.constCaching()) {
			il.add(loadCachedConst(node.value()));
		}
		else {
			il.add(ASMUtils.loadDouble(node.value()));
			il.add(BoxedPrimitivesMethods.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
		}
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(LoadConst.Str node) {
		il.add(new LdcInsnNode(node.value()));
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	private static String dispatchMethodName(BinOp.Op op) {
		switch (op) {
			case ADD:    return OP_ADD;
			case SUB:    return OP_SUB;
			case MUL:    return OP_MUL;
			case MOD:    return OP_MOD;
			case POW:    return OP_POW;
			case DIV:    return OP_DIV;
			case IDIV:   return OP_IDIV;
			case BAND:   return OP_BAND;
			case BOR:    return OP_BOR;
			case BXOR:   return OP_BXOR;
			case SHL:    return OP_SHL;
			case SHR:    return OP_SHR;

			case CONCAT: return OP_CONCAT;

			case EQ:     return OP_EQ;
			case NEQ:    return OP_NEQ;
			case LT:     return OP_LT;
			case LE:     return OP_LE;

			default:     throw new IllegalArgumentException("Illegal binary operation: " + op);
		}
	}

	private static String dispatchMethodName(UnOp.Op op) {
		switch (op) {
			case UNM:  return OP_UNM;
			case BNOT: return OP_BNOT;
			case LEN:  return OP_LEN;
			default:   throw new IllegalArgumentException("Illegal unary operation: " + op);
		}
	}

	@Override
	public void visit(BinOp node) {
		ResumptionPoint rp = newResumptionPoint();
		il.add(rp.save());

		il.add(loadExecutionContext());
		il.add(new VarInsnNode(ALOAD, slot(node.left())));
		il.add(new VarInsnNode(ALOAD, slot(node.right())));
		il.add(DispatchMethods.dynamic(dispatchMethodName(node.op()), 2));

		il.add(rp.resume());
		il.add(retrieve_0());
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(UnOp node) {
		if (node.op() == UnOp.Op.NOT) {
			il.add(new VarInsnNode(ALOAD, slot(node.arg())));
			il.add(ConversionMethods.booleanValueOf());
			il.add(new InsnNode(ICONST_1));
			il.add(new InsnNode(IXOR));
			il.add(BoxedPrimitivesMethods.box(Type.BOOLEAN_TYPE, Type.getType(Boolean.class)));
		}
		else {
			ResumptionPoint rp = newResumptionPoint();
			il.add(rp.save());

			il.add(loadExecutionContext());
			il.add(new VarInsnNode(ALOAD, slot(node.arg())));
			il.add(DispatchMethods.dynamic(dispatchMethodName(node.op()), 1));

			il.add(rp.resume());
			il.add(retrieve_0());
		}

		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(TabNew node) {
		il.add(loadExecutionContext());
		il.add(loadState());
		il.add(LuaStateMethods.newTable(node.array(), node.hash()));
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(TabGet node) {
		ResumptionPoint rp = newResumptionPoint();
		il.add(rp.save());

		il.add(loadExecutionContext());
		il.add(new VarInsnNode(ALOAD, slot(node.obj())));
		il.add(new VarInsnNode(ALOAD, slot(node.key())));
		il.add(DispatchMethods.index());

		il.add(rp.resume());
		il.add(retrieve_0());
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(TabSet node) {
		ResumptionPoint rp = newResumptionPoint();
		il.add(rp.save());

		il.add(loadExecutionContext());
		il.add(new VarInsnNode(ALOAD, slot(node.obj())));
		il.add(new VarInsnNode(ALOAD, slot(node.key())));
		il.add(new VarInsnNode(ALOAD, slot(node.value())));
		il.add(DispatchMethods.setindex());

		il.add(rp.resume());
	}

	@Override
	public void visit(TabRawSet node) {
		il.add(new VarInsnNode(ALOAD, slot(node.obj())));
		il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Table.class)));
		il.add(new VarInsnNode(ALOAD, slot(node.key())));
		il.add(new VarInsnNode(ALOAD, slot(node.value())));
		il.add(TableMethods.rawset());
	}

	@Override
	public void visit(TabRawSetInt node) {
		il.add(new VarInsnNode(ALOAD, slot(node.obj())));
		il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Table.class)));
		il.add(ASMUtils.loadInt(node.idx()));
		il.add(new VarInsnNode(ALOAD, slot(node.value())));
		il.add(TableMethods.rawset_int());
	}

	@Override
	public void visit(TabRawAppendMulti node) {
		/*
		 In Java terms, we're translating this into the following loop:

			Table tab;
			ReturnBuffer rbuf = context.getReturnBuffer();
			int i = 0;
			context.registerTicks(rbuf.size());  // only when we care about ticks spent
			while (i < rbuf.size()) {
				tab.rawset(OFFSET + i, rbuf.get(i));
				i++;
			}
		*/

		LabelNode begin = new LabelNode();
		LabelNode end = new LabelNode();
		LabelNode top = new LabelNode();

		int lv_idx_tab = nextLocalVariableIndex();
		int lv_idx_stack = nextLocalVariableIndex() + 1;
		int lv_idx_i = nextLocalVariableIndex() + 2;

		locals.add(new LocalVariableNode("tab", Type.getDescriptor(Table.class), null, begin, end, lv_idx_tab));
		locals.add(new LocalVariableNode("rbuf", Type.getDescriptor(ReturnBuffer.class), null, begin, end, lv_idx_stack));
		locals.add(new LocalVariableNode("i", Type.INT_TYPE.getDescriptor(), null, begin, end, lv_idx_i));

		il.add(begin);

		il.add(new VarInsnNode(ALOAD, slot(node.obj())));
		il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Table.class)));
		il.add(new VarInsnNode(ASTORE, lv_idx_tab));

		il.add(loadExecutionContext());
		il.add(loadReturnBuffer());
		il.add(new VarInsnNode(ASTORE, lv_idx_stack));

		il.add(ASMUtils.loadInt(0));
		il.add(new VarInsnNode(ISTORE, lv_idx_i));

		// context.registerTicks(rbuf.size());
		if (countingTicks()) {
			il.add(loadExecutionContext());
			il.add(new VarInsnNode(ALOAD, lv_idx_stack));
			il.add(ReturnBufferMethods.size());
			il.add(ExecutionContextMethods.registerTicks());
		}

		il.add(top);
		il.add(new FrameNode(F_APPEND, 3, new Object[] {
					Type.getInternalName(Table.class),
					Type.getInternalName(ReturnBuffer.class),
					Opcodes.INTEGER
				}, 0, null));

		il.add(new VarInsnNode(ILOAD, lv_idx_i));
		il.add(new VarInsnNode(ALOAD, lv_idx_stack));
		il.add(ReturnBufferMethods.size());
		il.add(new JumpInsnNode(IF_ICMPGE, end));

		il.add(new VarInsnNode(ALOAD, lv_idx_tab));

		// OFFSET + i
		il.add(ASMUtils.loadInt(node.firstIdx()));
		il.add(new VarInsnNode(ILOAD, lv_idx_i));
		il.add(new InsnNode(IADD));

		// stack.get(i)
		il.add(new VarInsnNode(ALOAD, lv_idx_stack));
		il.add(new VarInsnNode(ILOAD, lv_idx_i));
		il.add(ReturnBufferMethods.get());

		// tab.rawset(offset + i, stack.get(i))
		il.add(TableMethods.rawset_int());

		// increment i
		il.add(new IincInsnNode(lv_idx_i, 1));

		il.add(new JumpInsnNode(GOTO, top));


		il.add(end);
		il.add(new FrameNode(F_CHOP, 3, null, 0, null));
	}

	@Override
	public void visit(Vararg node) {
		il.add(loadExecutionContext());
		il.add(loadReturnBuffer());
		il.add(new VarInsnNode(ALOAD, runMethod.LV_VARARGS));
		il.add(ReturnBufferMethods.setTo(0));
	}

	private int loadVList(VList vl, int maxKind) {
		if (vl.isMulti()) {
			// variable number of arguments, stored on stack

			if (vl.addrs().size() == 0) {
				// no prefix, simply take the stack contents as an array
				il.add(loadExecutionContext());
				il.add(loadReturnBuffer());
				il.add(ReturnBufferMethods.toArray());
				return 0;
			}
			else {
				// a non-empty prefix followed by the stack contents

				LabelNode begin = new LabelNode();
				LabelNode end = new LabelNode();

				int lv_idx_stack = nextLocalVariableIndex();
				int lv_idx_args = nextLocalVariableIndex() + 1;

				Type arrayType = ASMUtils.arrayTypeFor(Object.class);

				locals.add(new LocalVariableNode("stack", arrayType.getDescriptor(), null, begin, end, lv_idx_stack));
				locals.add(new LocalVariableNode("args", arrayType.getDescriptor(), null, begin, end, lv_idx_args));

				il.add(begin);

				// get stack contents as an array
				il.add(loadExecutionContext());
				il.add(loadReturnBuffer());
				il.add(ReturnBufferMethods.toArray());
				il.add(new VarInsnNode(ASTORE, lv_idx_stack));

				// compute the overall arg list length
				il.add(new VarInsnNode(ALOAD, lv_idx_stack));
				il.add(new InsnNode(ARRAYLENGTH));
				il.add(ASMUtils.loadInt(vl.addrs().size()));
				il.add(new InsnNode(IADD));

				// instantiate the actual arg list (length is on stack top)
				il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));
				il.add(new VarInsnNode(ASTORE, lv_idx_args));

				// fill in the prefix
				int idx = 0;
				for (Val v : vl.addrs()) {
					il.add(new VarInsnNode(ALOAD, lv_idx_args));
					il.add(ASMUtils.loadInt(idx++));
					il.add(new VarInsnNode(ALOAD, slot(v)));
					il.add(new InsnNode(AASTORE));
				}

				// call System.arraycopy(stack, 0, args, prefix_length, stack.length)
				il.add(new VarInsnNode(ALOAD, lv_idx_stack));
				il.add(ASMUtils.loadInt(0));
				il.add(new VarInsnNode(ALOAD, lv_idx_args));
				il.add(ASMUtils.loadInt(vl.addrs().size()));
				il.add(new VarInsnNode(ALOAD, lv_idx_stack));
				il.add(new InsnNode(ARRAYLENGTH));
				il.add(new MethodInsnNode(
						INVOKESTATIC,
						Type.getInternalName(System.class),
						"arraycopy",
						Type.getMethodDescriptor(
								Type.VOID_TYPE,
								Type.getType(Object.class), Type.INT_TYPE, Type.getType(Object.class), Type.INT_TYPE, Type.INT_TYPE),
						false));

				// push result to stack
				il.add(new VarInsnNode(ALOAD, lv_idx_args));
				il.add(end);

				return 0;
			}
		}
		else {
			// fixed number of arguments

			int k = vl.addrs().size() + 1;
			if (k <= maxKind) {
				// pass arguments on the JVM stack
				for (Val v : vl.addrs()) {
					il.add(new VarInsnNode(ALOAD, slot(v)));
				}
				return k;
			}
			else {
				// pass arguments packed in an array
				il.add(ASMUtils.loadInt(vl.addrs().size()));
				il.add(new TypeInsnNode(ANEWARRAY, Type.getInternalName(Object.class)));

				int idx = 0;
				for (Val v : vl.addrs()) {
					il.add(new InsnNode(DUP));
					il.add(ASMUtils.loadInt(idx++));
					il.add(new VarInsnNode(ALOAD, slot(v)));
					il.add(new InsnNode(AASTORE));
				}

				return 0;
			}
		}
	}

	@Override
	public void visit(Ret node) {
		il.add(loadExecutionContext());
		il.add(loadReturnBuffer());
		int kind = loadVList(node.args(), ReturnBufferMethods.MAX_SETTO_KIND);  // values
		il.add(ReturnBufferMethods.setTo(kind));
		il.add(_return());
	}

	@Override
	public void visit(TCall node) {
		il.add(loadExecutionContext());
		il.add(loadReturnBuffer());
		il.add(new VarInsnNode(ALOAD, slot(node.target())));  // call target
		int kind = loadVList(node.args(), ReturnBufferMethods.MAX_TAILCALL_KIND);  // call args
		il.add(ReturnBufferMethods.tailCall(kind));
		il.add(_return());
	}

	@Override
	public void visit(Call node) {
		ResumptionPoint rp = newResumptionPoint();
		il.add(rp.save());

		il.add(loadExecutionContext());
		il.add(new VarInsnNode(ALOAD, slot(node.fn())));  // call target
		int kind = loadVList(node.args(), DispatchMethods.MAX_CALL_KIND);  // call args
		il.add(DispatchMethods.call(kind));

		il.add(rp.resume());
	}

	@Override
	public void visit(MultiGet node) {
		il.add(loadExecutionContext());
		il.add(loadReturnBuffer());
		il.add(ReturnBufferMethods.get(node.idx()));
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(Label node) {
		il.add(l(node));
		il.add(ASMUtils.frameSame());
	}

	@Override
	public void visit(Jmp node) {
		il.add(_goto(node.jmpDest()));
	}

	private class ClosureUse {

		private final FunctionId id;
		private final List<AbstractVar> upvals;

		private final String fieldName;  // may be null

		private ClosureUse(FunctionId id, List<AbstractVar> upvals, int idx) {
			this.id = Check.notNull(id);
			this.upvals = Check.notNull(upvals);

			if (isClosed() && !isPure()) {
				this.fieldName = context.addFieldName("c_" + idx);
			}
			else {
				this.fieldName = null;
			}
		}

		public boolean isClosed() {
			for (AbstractVar uv : upvals) {
				if (uv instanceof Var) {
					return false;
				}
			}
			return true;
		}

		public boolean isPure() {
			return upvals.isEmpty();
		}

		public RunMethod.ClosureFieldInstance toClosureFieldInstance() {
			assert (this.isClosed());

			FieldNode fieldNode = instanceFieldNode();

			InsnList il = new InsnList();
			il.add(new VarInsnNode(ALOAD, 0));
			il.add(instantiationInsns());
			il.add(new FieldInsnNode(
					PUTFIELD,
					context.thisClassType().getInternalName(),
					instanceFieldName(),
					instanceType().getDescriptor()));

			return new RunMethod.ClosureFieldInstance(instanceFieldNode(), il);
		}

		private InsnList instantiationInsns() {
			InsnList il = new InsnList();

			ClassNameTranslator tr = context.classNameTranslator;

			Type fnType = ASMUtils.typeForClassName(id.toClassName(tr));

			il.add(new TypeInsnNode(NEW, fnType.getInternalName()));
			il.add(new InsnNode(DUP));
			for (AbstractVar var : upvals) {
				if (var instanceof UpVar) {
					il.add(loadUpvalueRef((UpVar) var));
				}
				else {
					Var v = (Var) var;
					assert (context.types.isReified(v));
					il.add(new VarInsnNode(ALOAD, slot(v)));
					il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Variable.class)));
				}
			}

			Type[] ctorArgTypes = new Type[upvals.size()];
			Arrays.fill(ctorArgTypes, Type.getType(Variable.class));

			il.add(ASMUtils.ctor(fnType, ctorArgTypes));

			return il;
		}

		private String instanceFieldName() {
			return fieldName;
		}

		private Type instanceType() {
			return ASMUtils.typeForClassName(id.toClassName(context.classNameTranslator));
		}

		private FieldNode instanceFieldNode() {
			return new FieldNode(
					ACC_PRIVATE + ACC_FINAL,
					instanceFieldName(),
					instanceType().getDescriptor(),
					null,
					null);
		}

		private InsnList fetchInstanceInsns() {
			InsnList il = new InsnList();

			if (this.isClosed()) {
				if (this.isPure()) {
					il.add(new FieldInsnNode(
							GETSTATIC,
							instanceType().getInternalName(),
							ASMBytecodeEmitter.instanceFieldName(),
							instanceType().getDescriptor()));
				}
				else {
					il.add(new VarInsnNode(ALOAD, 0));
					il.add(new FieldInsnNode(
							GETFIELD,
							context.thisClassType().getInternalName(),
							instanceFieldName(),
							instanceType().getDescriptor()));
				}
			}
			else {
				il.add(instantiationInsns());
			}

			return il;
		}

	}

	@Override
	public void visit(Closure node) {
		ClosureUse cu = new ClosureUse(node.id(), node.args(), instanceLevelClosures.size());

		if (cu.isClosed() && !cu.isPure()) {
			instanceLevelClosures.add(cu.toClosureFieldInstance());
		}

		il.add(cu.fetchInstanceInsns());
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(ToNumber node) {
		il.add(new VarInsnNode(ALOAD, slot(node.src())));
		il.add(ConversionMethods.toNumericalValue(node.desc()));
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(ToNext node) {
		il.add(_next(node.label()));
	}

	private Label destLabel;

	@Override
	public void visit(Branch branch) {
		assert (destLabel == null);

		try {
			destLabel = branch.jmpDest();
			branch.condition().accept(this);
			il.add(_next(branch.next()));
		}
		finally {
			destLabel = null;
		}
	}

	@Override
	public void visit(Branch.Condition.Nil cond) {
		assert (destLabel != null);
		il.add(new VarInsnNode(ALOAD, slot(cond.addr())));

		if (!isSub() || resolver.isLocalLabel(destLabel)) {
			// local jump
			il.add(new JumpInsnNode(IFNULL, l(destLabel)));
		}
		else {
			// non-local jump
			LabelNode l_nojump = new LabelNode();
			il.add(new JumpInsnNode(IFNONNULL, l_nojump));
			il.add(_nonLocalGoto(destLabel));
			il.add(l_nojump);
			il.add(new FrameNode(F_SAME, 0, null, 0, null));
		}
	}

	@Override
	public void visit(Branch.Condition.Bool cond) {
		assert (destLabel != null);
		il.add(new VarInsnNode(ALOAD, slot(cond.addr())));
		il.add(ConversionMethods.booleanValueOf());

		if (!isSub() || resolver.isLocalLabel(destLabel)) {
			// local jump
			il.add(new JumpInsnNode(cond.expected() ? IFNE : IFEQ, l(destLabel)));
		}
		else {
			// non-local jump
			LabelNode l_nojump = new LabelNode();
			il.add(new JumpInsnNode(cond.expected() ? IFEQ : IFNE, l_nojump));
			il.add(_nonLocalGoto(destLabel));
			il.add(l_nojump);
			il.add(new FrameNode(F_SAME, 0, null, 0, null));
		}

	}

	@Override
	public void visit(Branch.Condition.NumLoopEnd cond) {
		assert (destLabel != null);
		il.add(new VarInsnNode(ALOAD, slot(cond.var())));
		il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Number.class)));
		il.add(new VarInsnNode(ALOAD, slot(cond.limit())));
		il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Number.class)));
		il.add(new VarInsnNode(ALOAD, slot(cond.step())));
		il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Number.class)));
		il.add(DispatchMethods.continueLoop());

		if (!isSub() || resolver.isLocalLabel(destLabel)) {
			// local jump
			il.add(new JumpInsnNode(IFEQ, l(destLabel)));
		}
		else {
			// non-local jump
			LabelNode l_nojump = new LabelNode();
			il.add(new JumpInsnNode(IFNE, l_nojump));
			il.add(_nonLocalGoto(destLabel));
			il.add(l_nojump);
			il.add(new FrameNode(F_SAME, 0, null, 0, null));
		}
	}

	private void staticCpuWithdraw(int cost) {
		switch (context.compilerSettings.cpuAccountingMode()) {
			case NO_CPU_ACCOUNTING: {
				// no-op
				break;
			}

			case IN_EVERY_BASIC_BLOCK: {
				ResumptionPoint rp = newResumptionPoint();
				il.add(rp.save());

				il.add(loadExecutionContext());
				il.add(new InsnNode(DUP));
				il.add(ASMUtils.loadInt(cost));
				il.add(ExecutionContextMethods.registerTicks());
				il.add(ExecutionContextMethods.checkCallYield());

				il.add(rp.resume());
				break;
			}

			default: throw new UnsupportedOperationException("Unsupported CPU accounting mode: " + context.compilerSettings.cpuAccountingMode());
		}
	}

	// do we care about counting ticks?
	private boolean countingTicks() {
		return (context.compilerSettings.cpuAccountingMode()
				!= CompilerSettings.CPUAccountingMode.NO_CPU_ACCOUNTING);
	}

	@Override
	public void visit(CPUWithdraw node) {
		staticCpuWithdraw(node.cost());
	}

	@Override
	public void visit(Line node) {
		LabelNode l = new LabelNode();
		il.add(l);
		il.add(new LineNumberNode(node.lineNumber(), l));
	}

}

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
import net.sandius.rembulan.compiler.gen.asm.helpers.ObjectSinkMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.TableMethods;
import net.sandius.rembulan.compiler.gen.asm.helpers.VariableMethods;
import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Variable;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.sandius.rembulan.compiler.gen.asm.helpers.DispatchMethods.*;
import static org.objectweb.asm.Opcodes.*;

class BytecodeEmitVisitor extends CodeVisitor {

	private final ASMBytecodeEmitter context;
	private final RunMethod runMethod;

	private final SlotAllocInfo slots;
	private final TypeInfo types;

	private final Map<Object, LabelNode> labels;
	private final ArrayList<LabelNode> resumptionPoints;

	private final InsnList il;
	private final List<LocalVariableNode> locals;

	private int closureIdx;
	private final List<RunMethod.ClosureFieldInstance> instanceLevelClosures;

	private int constIdx;
	private final List<RunMethod.ConstFieldInstance> constFields;

	public BytecodeEmitVisitor(ASMBytecodeEmitter context, RunMethod runMethod, SlotAllocInfo slots, TypeInfo types) {
		this.context = Check.notNull(context);
		this.runMethod = Check.notNull(runMethod);
		this.slots = Check.notNull(slots);
		this.types = Check.notNull(types);

		this.labels = new HashMap<>();
		this.resumptionPoints = new ArrayList<>();

		this.il = new InsnList();
		this.locals = new ArrayList<>();

		this.closureIdx = 0;
		this.instanceLevelClosures = new ArrayList<>();

		this.constIdx = 0;
		this.constFields = new ArrayList<>();
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

	private RunMethod.ConstFieldInstance newConstFieldInstance(Object constValue) {
		Check.notNull(constValue);

		String fieldName = "_k_" + (constIdx++);

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

		RunMethod.ConstFieldInstance cfi = newConstFieldInstance(constValue);
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

	static AbstractInsnNode loadSink() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				Type.getInternalName(ExecutionContext.class),
				"getObjectSink",
				Type.getMethodDescriptor(
						Type.getType(ObjectSink.class)),
				true);
	}

	public InsnList retrieve_0() {
		InsnList il = new InsnList();

		il.add(loadExecutionContext());
		il.add(loadSink());
		il.add(ObjectSinkMethods.get(0));

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

	class ResumptionPoint {

		public final int index;

		private ResumptionPoint(int index) {
			this.index = index;
		}

		public LabelNode label() {
			return l(this);
		}

		public InsnList save() {
			InsnList il = new InsnList();
			il.add(ASMUtils.loadInt(index + 1));
			il.add(new VarInsnNode(ISTORE, runMethod.LV_RESUME));
			return il;
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
		return resumptionPoints.size() > 0;
	}

	public List<LabelNode> resumptionLabels() {
		return resumptionPoints;
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
		il.add(DispatchMethods.newindex());

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
			ObjectSink stack = context.getObjectSink();
			int i = 0;
			while (i < stack.size()) {
				tab.rawset(OFFSET + i, stack.get(i));
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
		locals.add(new LocalVariableNode("stack", Type.getDescriptor(ObjectSink.class), null, begin, end, lv_idx_stack));
		locals.add(new LocalVariableNode("i", Type.INT_TYPE.getDescriptor(), null, begin, end, lv_idx_i));

		il.add(begin);

		il.add(new VarInsnNode(ALOAD, slot(node.obj())));
		il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Table.class)));
		il.add(new VarInsnNode(ASTORE, lv_idx_tab));

		il.add(loadExecutionContext());
		il.add(loadSink());
		il.add(new VarInsnNode(ASTORE, lv_idx_stack));

		il.add(ASMUtils.loadInt(0));
		il.add(new VarInsnNode(ISTORE, lv_idx_i));

		il.add(top);
		il.add(new FrameNode(F_APPEND, 3, new Object[] {
					Type.getInternalName(Table.class),
					Type.getInternalName(ObjectSink.class),
					Opcodes.INTEGER
				}, 0, null));

		il.add(new VarInsnNode(ILOAD, lv_idx_i));
		il.add(new VarInsnNode(ALOAD, lv_idx_stack));
		il.add(ObjectSinkMethods.size());
		il.add(new JumpInsnNode(IF_ICMPGE, end));

		il.add(new VarInsnNode(ALOAD, lv_idx_tab));

		// OFFSET + i
		il.add(ASMUtils.loadInt(node.firstIdx()));
		il.add(new VarInsnNode(ILOAD, lv_idx_i));
		il.add(new InsnNode(IADD));

		// stack.get(i)
		il.add(new VarInsnNode(ALOAD, lv_idx_stack));
		il.add(new VarInsnNode(ILOAD, lv_idx_i));
		il.add(ObjectSinkMethods.get());

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
		il.add(loadSink());
		il.add(new VarInsnNode(ALOAD, runMethod.LV_VARARGS));
		il.add(ObjectSinkMethods.setTo(0));
	}

	private int loadVList(VList vl, int maxKind) {
		if (vl.isMulti()) {
			// variable number of arguments, stored on stack

			if (vl.addrs().size() == 0) {
				// no prefix, simply take the stack contents as an array
				il.add(loadExecutionContext());
				il.add(loadSink());
				il.add(ObjectSinkMethods.toArray());
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
				il.add(loadSink());
				il.add(ObjectSinkMethods.toArray());
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
		il.add(loadSink());
		int kind = loadVList(node.args(), ObjectSinkMethods.MAX_SETTO_KIND);  // values
		il.add(ObjectSinkMethods.setTo(kind));
		il.add(new InsnNode(RETURN));
	}

	@Override
	public void visit(TCall node) {
		il.add(loadExecutionContext());
		il.add(loadSink());
		il.add(new VarInsnNode(ALOAD, slot(node.target())));  // call target
		int kind = loadVList(node.args(), ObjectSinkMethods.MAX_TAILCALL_KIND);  // call args
		il.add(ObjectSinkMethods.tailCall(kind));
		il.add(new InsnNode(RETURN));
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
		il.add(loadSink());
		il.add(ObjectSinkMethods.get(node.idx()));
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(Label node) {
		il.add(l(node));
		il.add(ASMUtils.frameSame());
	}

	@Override
	public void visit(Jmp node) {
		il.add(new JumpInsnNode(GOTO, l(node.jmpDest())));
	}

	private class ClosureUse {

		private final FunctionId id;
		private final List<AbstractVar> upvals;

		private final String fieldName;  // may be null

		private ClosureUse(FunctionId id, List<AbstractVar> upvals) {
			this.id = Check.notNull(id);
			this.upvals = Check.notNull(upvals);

			if (isClosed() && !isPure()) {
				this.fieldName = context.addFieldName("c_" + (closureIdx++));
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
		ClosureUse cu = new ClosureUse(node.id(), node.args());

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
		// no-op
	}

	private LabelNode dest;

	@Override
	public void visit(Branch branch) {
		assert (dest == null);
		try {
			dest = l(branch.jmpDest());
			branch.condition().accept(this);
		}
		finally {
			dest = null;
		}
	}

	@Override
	public void visit(Branch.Condition.Nil cond) {
		assert (dest != null);
		il.add(new VarInsnNode(ALOAD, slot(cond.addr())));
		il.add(new JumpInsnNode(IFNULL, dest));
	}

	@Override
	public void visit(Branch.Condition.Bool cond) {
		assert (dest != null);
		il.add(new VarInsnNode(ALOAD, slot(cond.addr())));
		il.add(ConversionMethods.booleanValueOf());
		il.add(new JumpInsnNode(cond.expected() ? IFNE : IFEQ, dest));
	}

	@Override
	public void visit(Branch.Condition.NumLoopEnd cond) {
		assert (dest != null);
		il.add(new VarInsnNode(ALOAD, slot(cond.var())));
		il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Number.class)));
		il.add(new VarInsnNode(ALOAD, slot(cond.limit())));
		il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Number.class)));
		il.add(new VarInsnNode(ALOAD, slot(cond.step())));
		il.add(new TypeInsnNode(CHECKCAST, Type.getInternalName(Number.class)));
		il.add(DispatchMethods.continueLoop());
		il.add(new JumpInsnNode(IFEQ, dest));
	}

	@Override
	public void visit(CPUWithdraw node) {
		switch (context.compilerSettings.cpuAccountingMode()) {
			case NO_CPU_ACCOUNTING: {
				// no-op
				break;
			}

			case IN_EVERY_BASIC_BLOCK: {
				ResumptionPoint rp = newResumptionPoint();
				il.add(rp.save());

				il.add(loadExecutionContext());
				il.add(ExecutionContextMethods.checkCpu(node.cost()));

				il.add(rp.resume());
				break;
			}

			default: throw new UnsupportedOperationException("Unsupported CPU accounting mode: " + context.compilerSettings.cpuAccountingMode());
		}
	}

}

package net.sandius.rembulan.compiler.gen.mk2;

import net.sandius.rembulan.compiler.CodeVisitor;
import net.sandius.rembulan.compiler.analysis.SlotAllocInfo;
import net.sandius.rembulan.compiler.analysis.TypeInfo;
import net.sandius.rembulan.compiler.gen.asm.ASMUtils;
import net.sandius.rembulan.compiler.gen.asm.BoxedPrimitivesMethods;
import net.sandius.rembulan.compiler.gen.asm.ConversionMethods;
import net.sandius.rembulan.compiler.gen.asm.DispatchMethods;
import net.sandius.rembulan.compiler.gen.asm.LuaStateMethods;
import net.sandius.rembulan.compiler.gen.asm.ObjectSinkMethods;
import net.sandius.rembulan.compiler.gen.asm.UpvalueMethods;
import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.core.Conversions;
import net.sandius.rembulan.core.ExecutionContext;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.ObjectSink;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.util.Check;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static net.sandius.rembulan.compiler.gen.asm.DispatchMethods.*;
import static org.objectweb.asm.Opcodes.*;

class BytecodeEmitVisitor extends CodeVisitor {

	private final ASMBytecodeEmitter context;
	private final RunMethod runMethod;

	private final SlotAllocInfo slots;
	private final TypeInfo types;

	private final Map<Object, LabelNode> labels;
	private final ArrayList<LabelNode> resumptionPoints;

	private final InsnList il;

	public BytecodeEmitVisitor(ASMBytecodeEmitter context, RunMethod runMethod, SlotAllocInfo slots, TypeInfo types) {
		this.context = Check.notNull(context);
		this.runMethod = Check.notNull(runMethod);
		this.slots = Check.notNull(slots);
		this.types = Check.notNull(types);

		this.labels = new HashMap<>();
		this.resumptionPoints = new ArrayList<>();

		this.il = new InsnList();
	}

	public InsnList instructions() {
		return il;
	}

	protected int slot(AbstractVal v) {
		return runMethod.slotOffset() + slots.slotOf(v);
	}

	protected int slot(Var v) {
		return runMethod.slotOffset() + slots.slotOf(v);
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

	public AbstractInsnNode loadExecutionContext() {
		return new VarInsnNode(ALOAD, runMethod.LV_CONTEXT);
	}

	private AbstractInsnNode loadState() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				Type.getInternalName(ExecutionContext.class),
				"getState",
				Type.getMethodDescriptor(
						Type.getType(LuaState.class)),
				true);
	}

	private AbstractInsnNode loadSink() {
		return new MethodInsnNode(
				INVOKEINTERFACE,
				Type.getInternalName(ExecutionContext.class),
				"getObjectSink",
				Type.getMethodDescriptor(
						Type.getType(ObjectSink.class)),
				true);
	}

	private InsnList loadLuaState() {
		InsnList il = new InsnList();
		il.add(loadExecutionContext());
		il.add(loadState());
		return il;
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
				Type.getDescriptor(Upvalue.class)));

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
			il.add(ASMUtils.loadInt(index));
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

	public ResumptionPoint resumptionPoint() {
		int idx = resumptionPoints.size();
		ResumptionPoint rp = new ResumptionPoint(idx);
		resumptionPoints.add(rp.label());
		return rp;
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

	private void varStore(Val src, Var dest) {
		if (types.isReified(dest)) {
			il.add(new VarInsnNode(ALOAD, slot(dest)));
			il.add(new VarInsnNode(ALOAD, slot(src)));
			il.add(UpvalueMethods.set());
		}
		else {
			il.add(new VarInsnNode(ALOAD, slot(src)));
			il.add(new VarInsnNode(ASTORE, slot(dest)));
		}
	}

	@Override
	public void visit(VarInit node) {
		varStore(node.src(), node.var());
	}

	@Override
	public void visit(VarStore node) {
		varStore(node.src(), node.var());
	}

	@Override
	public void visit(VarLoad node) {
		if (types.isReified(node.var())) {
			il.add(new VarInsnNode(ALOAD, slot(node.var())));
			il.add(new TypeInsnNode(CHECKCAST, Type.getDescriptor(Upvalue.class)));
			il.add(UpvalueMethods.get());
		}
		else {
			il.add(new VarInsnNode(ALOAD, slot(node.var())));
		}
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(UpLoad node) {
		il.add(loadUpvalueRef(node.upval()));
		il.add(UpvalueMethods.get());
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(UpStore node) {
		il.add(loadUpvalueRef(node.upval()));
		il.add(new VarInsnNode(ALOAD, slot(node.src())));
		il.add(UpvalueMethods.set());
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
		il.add(ASMUtils.loadLong(node.value()));
		il.add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, Type.getType(Long.class)));
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(LoadConst.Flt node) {
		il.add(ASMUtils.loadDouble(node.value()));
		il.add(BoxedPrimitivesMethods.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(LoadConst.Str node) {
		il.add(new LdcInsnNode(node.value()));
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	private static String dispatchMethodName(BinOp.Op op) {
		switch (op) {
			case ADD:  return OP_ADD;
			case SUB:  return OP_SUB;
			case MUL:  return OP_MUL;
			case MOD:  return OP_MOD;
			case POW:  return OP_POW;
			case DIV:  return OP_DIV;
			case IDIV: return OP_IDIV;
			case BAND: return OP_BAND;
			case BOR:  return OP_BOR;
			case BXOR: return OP_BXOR;
			case SHL:  return OP_SHL;
			case SHR:  return OP_SHR;
			default:   throw new IllegalArgumentException("Illegal binary operation: " + op);
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
		ResumptionPoint rp = resumptionPoint();
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
			il.add(BoxedPrimitivesMethods.box(Type.BOOLEAN_TYPE, Type.getType(Boolean.class)));
		}
		else {
			ResumptionPoint rp = resumptionPoint();
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
		il.add(loadLuaState());
		il.add(LuaStateMethods.newTable(node.array(), node.hash()));
		il.add(new VarInsnNode(ASTORE, slot(node.dest())));
	}

	@Override
	public void visit(TabGet node) {
		ResumptionPoint rp = resumptionPoint();
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
		ResumptionPoint rp = resumptionPoint();
		il.add(rp.save());

		il.add(loadExecutionContext());
		il.add(new VarInsnNode(ALOAD, slot(node.obj())));
		il.add(new VarInsnNode(ALOAD, slot(node.key())));
		il.add(new VarInsnNode(ALOAD, slot(node.value())));
		il.add(DispatchMethods.newindex());

		il.add(rp.resume());
	}

	@Override
	public void visit(TabStackAppend node) {
		throw new UnsupportedOperationException(); // TODO
	}

	@Override
	public void visit(Vararg node) {
		throw new UnsupportedOperationException(); // TODO
	}

	@Override
	public void visit(Ret node) {
		// TODO
		il.add(new InsnNode(RETURN));
	}

	@Override
	public void visit(TCall node) {
		// TODO
		il.add(new InsnNode(RETURN));
	}

	@Override
	public void visit(Call node) {
		throw new UnsupportedOperationException(); // TODO
	}

	@Override
	public void visit(StackGet node) {
		throw new UnsupportedOperationException(); // TODO
	}

	@Override
	public void visit(Label node) {
		il.add(l(node));
	}

	@Override
	public void visit(Jmp node) {
		il.add(new JumpInsnNode(GOTO, l(node.jmpDest())));
	}

	@Override
	public void visit(Closure node) {
		throw new UnsupportedOperationException(); // TODO
	}

	@Override
	public void visit(ToNumber node) {
		il.add(new VarInsnNode(ALOAD, slot(node.dest())));
		il.add(ConversionMethods.toNumericalValue(""));
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
		il.add(new VarInsnNode(ALOAD, slot(cond.limit())));
		il.add(new VarInsnNode(ALOAD, slot(cond.step())));
		il.add(DispatchMethods.continueLoop());
		il.add(new JumpInsnNode(IFEQ, dest));
	}

	@Override
	public void visit(CPUWithdraw node) {
		// TODO
	}

}

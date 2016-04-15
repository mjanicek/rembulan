package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.compiler.gen.CodeVisitor;
import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.compiler.gen.block.LuaBinaryOperation;
import net.sandius.rembulan.compiler.gen.block.LuaInstruction;
import net.sandius.rembulan.compiler.gen.block.StaticMathImplementation;
import net.sandius.rembulan.core.RawOperators;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntIterable;
import net.sandius.rembulan.util.IntIterator;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static org.objectweb.asm.Opcodes.*;

public class JavaBytecodeCodeVisitor extends CodeVisitor {

	private final CodeEmitter e;

	public JavaBytecodeCodeVisitor(CodeEmitter e) {
		this.e = Check.notNull(e);
	}

	protected void add(InsnList il) {
		e.code().add(il);
	}

	protected void add(AbstractInsnNode il) {
		e.code().add(il);
	}

	@Override
	public void visitTarget(Object id) {
		add(e._l(id));
		add(ASMUtils.frameSame());
	}

	@Override
	public void visitJump(Object id, Object target) {
		add(new JumpInsnNode(GOTO, e._l(target)));
	}

	@Override
	public void visitCapture(Object it, SlotState st, IntIterable indices) {
		IntIterator iit = indices.iterator();
		while (iit.hasNext()) {
			int idx = iit.next();
			add(e.captureRegister(idx));
		}
	}

	@Override
	public void visitCloseUpvalues(Object it, SlotState st, int fromIndex) {
		for (int i = fromIndex; i < st.size(); i++) {
			if (st.isCaptured(i)) {
				add(e.uncaptureRegister(i));
			}
		}
	}

	@Override
	public void visitMove(Object id, SlotState st, int r_src, int r_dest) {
		add(e.loadRegister(r_src, st));
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitLoadK(Object id, SlotState st, int r_dest, int constIndex) {
		add(e.loadConstant(constIndex));
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitLoadBool(Object id, SlotState st, int r_dest, boolean value) {
		add(BoxedPrimitivesMethods.loadBoxedBoolean(value));
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitLoadNil(Object id, SlotState st, int r_dest, int count) {
		for (int i = 0; i < count; i++) {
			add(BoxedPrimitivesMethods.loadNull());
			add(e.storeToRegister(r_dest + i, st));
		}
	}

	@Override
	public void visitGetUpVal(Object id, SlotState st, int r_dest, int upvalueIndex) {
		add(e.getUpvalueReference(upvalueIndex));
		add(UpvalueMethods.get());
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitGetTabUp(Object id, SlotState st, int r_dest, int upvalueIndex, int rk_key) {
		CodeEmitter.ResumptionPoint rp = e.resumptionPoint();
		add(rp.save());

		add(e.loadDispatchPreamble());
		add(e.getUpvalueReference(upvalueIndex));
		add(UpvalueMethods.get());
		add(e.loadRegisterOrConstant(rk_key, st));
		add(DispatchMethods.index());

		add(rp.resume());
		add(e.retrieve_0());
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitGetTable(Object id, SlotState st, int r_dest, int r_tab, int rk_key) {
		CodeEmitter.ResumptionPoint rp = e.resumptionPoint();
		add(rp.save());

		add(e.loadDispatchPreamble());
		add(e.loadRegister(r_tab, st));
		add(e.loadRegisterOrConstant(rk_key, st));
		add(DispatchMethods.index());

		add(rp.resume());
		add(e.retrieve_0());
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitSetTabUp(Object id, SlotState st, int upvalueIndex, int rk_key, int rk_value) {
		CodeEmitter.ResumptionPoint rp = e.resumptionPoint();
		add(rp.save());

		add(e.loadDispatchPreamble());
		add(e.getUpvalueReference(upvalueIndex));
		add(UpvalueMethods.get());
		add(e.loadRegisterOrConstant(rk_key, st));
		add(e.loadRegisterOrConstant(rk_value, st));
		add(DispatchMethods.newindex());

		add(rp.resume());
	}

	@Override
	public void visitSetUpVal(Object id, SlotState st, int r_src, int upvalueIndex) {
		add(e.getUpvalueReference(upvalueIndex));
		add(e.loadRegister(r_src, st));
		add(UpvalueMethods.set());
	}

	@Override
	public void visitSetTable(Object id, SlotState st, int r_tab, int rk_key, int rk_value) {
		CodeEmitter.ResumptionPoint rp = e.resumptionPoint();
		add(rp.save());

		add(e.loadDispatchPreamble());
		add(e.loadRegister(r_tab, st));
		add(e.loadRegisterOrConstant(rk_key, st));
		add(e.loadRegisterOrConstant(rk_value, st));
		add(DispatchMethods.newindex());

		add(rp.resume());
	}

	@Override
	public void visitNewTable(Object id, SlotState st, int r_dest, int arraySize, int hashSize) {
		add(e.loadLuaState());
		add(LuaStateMethods.newTable(arraySize, hashSize));
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitSelf(Object id, SlotState st, int r_dest, int r_self, int rk_key) {
		CodeEmitter.ResumptionPoint rp = e.resumptionPoint();
		add(rp.save());

		add(e.loadDispatchPreamble());

		add(e.loadRegister(r_self, st));
		add(new InsnNode(DUP));
		add(e.storeToRegister(r_dest + 1, st));

		add(e.loadRegisterOrConstant(rk_key, st));
		add(DispatchMethods.index());

		add(rp.resume());
		add(e.retrieve_0());
		add(e.storeToRegister(r_dest, st));
	}

	protected InsnList nativeBinaryOperationAndBox(int opcode, boolean resultIsLong) {
		InsnList il = new InsnList();

		il.add(new InsnNode(opcode));
		if (resultIsLong) {
			il.add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, Type.getType(Long.class)));
		}
		else {
			il.add(BoxedPrimitivesMethods.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
		}

		return il;
	}

	protected InsnList rawBinaryOperationAndBox(String name, boolean argsAreLong, boolean resultIsLong) {
		InsnList il = new InsnList();

		il.add(new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(RawOperators.class),
				name,
				Type.getMethodDescriptor(
						resultIsLong ? Type.LONG_TYPE : Type.DOUBLE_TYPE,
						argsAreLong ? Type.LONG_TYPE : Type.DOUBLE_TYPE,
						argsAreLong ? Type.LONG_TYPE : Type.DOUBLE_TYPE),
				false));
		if (resultIsLong) {
			il.add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, Type.getType(Long.class)));
		}
		else {
			il.add(BoxedPrimitivesMethods.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
		}
		return il;
	}

	protected InsnList binaryIntegerOperation(LuaBinaryOperation.Op op, SlotState s, int r_dest, int rk_left, int rk_right) {
		InsnList il = new InsnList();
		
		switch (op) {
			case DIV:
			case POW:
				il.add(e.loadNumericRegisterOrConstantValue(rk_left, s, Type.DOUBLE_TYPE));
				il.add(e.loadNumericRegisterOrConstantValue(rk_right, s, Type.DOUBLE_TYPE));
				break;

			case SHL:
			case SHR:
				il.add(e.loadNumericRegisterOrConstantValue(rk_left, s, Type.LONG_TYPE));
				il.add(e.loadNumericRegisterOrConstantValue(rk_right, s, Type.INT_TYPE));
				break;

			default:
				il.add(e.loadNumericRegisterOrConstantValue(rk_left, s, Type.LONG_TYPE));
				il.add(e.loadNumericRegisterOrConstantValue(rk_right, s, Type.LONG_TYPE));
				break;
		}

		switch (op) {
			case ADD:  il.add(nativeBinaryOperationAndBox(LADD, true)); break;
			case SUB:  il.add(nativeBinaryOperationAndBox(LSUB, true)); break;
			case MUL:  il.add(nativeBinaryOperationAndBox(LMUL, true)); break;
			case MOD:  il.add(rawBinaryOperationAndBox("rawmod", true, true)); break;
			case POW:  il.add(rawBinaryOperationAndBox("rawpow", false, false)); break;
			case DIV:  il.add(nativeBinaryOperationAndBox(DDIV, false)); break;
			case IDIV: il.add(rawBinaryOperationAndBox("rawidiv", true, true)); break;
			case BAND: il.add(nativeBinaryOperationAndBox(LAND, true)); break;
			case BOR:  il.add(nativeBinaryOperationAndBox(LOR, true)); break;
			case BXOR: il.add(nativeBinaryOperationAndBox(LXOR, true)); break;
			case SHL:  il.add(nativeBinaryOperationAndBox(LSHL, true)); break;
			case SHR:  il.add(nativeBinaryOperationAndBox(LUSHR, true)); break;
			default: throw new IllegalStateException("Illegal op: " + op);
		}

		il.add(e.storeToRegister(r_dest, s));

		return il;
	}

	protected InsnList binaryFloatOperation(LuaBinaryOperation.Op op, SlotState s, int r_dest, int rk_left, int rk_right) {
		InsnList il = new InsnList();

		il.add(e.loadNumericRegisterOrConstantValue(rk_left, s, Type.DOUBLE_TYPE));
		il.add(e.loadNumericRegisterOrConstantValue(rk_right, s, Type.DOUBLE_TYPE));

		switch (op) {
			case ADD:  il.add(nativeBinaryOperationAndBox(DADD, false)); break;
			case SUB:  il.add(nativeBinaryOperationAndBox(DSUB, false)); break;
			case MUL:  il.add(nativeBinaryOperationAndBox(DMUL, false)); break;
			case MOD:  il.add(rawBinaryOperationAndBox("rawmod", false, false)); break;
			case POW:  il.add(rawBinaryOperationAndBox("rawpow", false, false)); break;
			case DIV:  il.add(nativeBinaryOperationAndBox(DDIV, false)); break;
			case IDIV: il.add(rawBinaryOperationAndBox("rawidiv", false, false)); break;
			default: throw new IllegalStateException("Illegal op: " + op);
		}

		il.add(e.storeToRegister(r_dest, s));

		return il;
	}

	protected InsnList binaryNumericOperation(LuaBinaryOperation.Op op, SlotState s, int r_dest, int rk_left, int rk_right) {
		InsnList il = new InsnList();

		String method = op.name().toLowerCase();  // FIXME: brittle
		il.add(e.loadRegisterOrConstant(rk_left, s, Number.class));
		il.add(e.loadRegisterOrConstant(rk_right, s, Number.class));
		il.add(DispatchMethods.numeric(method, 2));
		il.add(e.storeToRegister(r_dest, s));

		return il;
	}

	protected InsnList binaryDynamicOperation(LuaBinaryOperation.Op op, SlotState s, int r_dest, int rk_left, int rk_right) {
		InsnList il = new InsnList();

		String method = op.name().toLowerCase();  // FIXME: brittle

		CodeEmitter.ResumptionPoint rp = e.resumptionPoint();
		il.add(rp.save());

		il.add(e.loadDispatchPreamble());
		il.add(e.loadRegisterOrConstant(rk_left, s));
		il.add(e.loadRegisterOrConstant(rk_right, s));
		il.add(DispatchMethods.dynamic(method, 2));

		il.add(rp.resume());
		il.add(e.retrieve_0());
		il.add(e.storeToRegister(r_dest, s));

		return il;
	}

	protected InsnList binaryOperation(LuaBinaryOperation.Op op, SlotState s, int r_dest, int rk_left, int rk_right) {
		InsnList il = new InsnList();

		StaticMathImplementation staticMath = LuaBinaryOperation.mathForOp(op);
		LuaInstruction.NumOpType ot = staticMath.opType(
				LuaBinaryOperation.slotType(e.context(), s, rk_left),
				LuaBinaryOperation.slotType(e.context(), s, rk_right));

		switch (ot) {
			case Integer: il.add(binaryIntegerOperation(op, s, r_dest, rk_left, rk_right)); break;
			case Float:   il.add(binaryFloatOperation(op, s, r_dest, rk_left, rk_right)); break;
			case Number:  il.add(binaryNumericOperation(op, s, r_dest, rk_left, rk_right)); break;
			case Any:     il.add(binaryDynamicOperation(op, s, r_dest, rk_left, rk_right)); break;
		}

		return il;
	}
	
	@Override
	public void visitAdd(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		add(binaryOperation(LuaBinaryOperation.Op.ADD, st, r_dest, rk_left, rk_right));
	}

	@Override
	public void visitSub(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		add(binaryOperation(LuaBinaryOperation.Op.SUB, st, r_dest, rk_left, rk_right));
	}

	@Override
	public void visitMul(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		add(binaryOperation(LuaBinaryOperation.Op.MUL, st, r_dest, rk_left, rk_right));
	}

	@Override
	public void visitMod(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		add(binaryOperation(LuaBinaryOperation.Op.MOD, st, r_dest, rk_left, rk_right));
	}

	@Override
	public void visitPow(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		add(binaryOperation(LuaBinaryOperation.Op.POW, st, r_dest, rk_left, rk_right));
	}

	@Override
	public void visitDiv(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		add(binaryOperation(LuaBinaryOperation.Op.DIV, st, r_dest, rk_left, rk_right));
	}

	@Override
	public void visitIDiv(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		add(binaryOperation(LuaBinaryOperation.Op.IDIV, st, r_dest, rk_left, rk_right));
	}

	@Override
	public void visitBAnd(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		add(binaryOperation(LuaBinaryOperation.Op.BAND, st, r_dest, rk_left, rk_right));
	}

	@Override
	public void visitBOr(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		add(binaryOperation(LuaBinaryOperation.Op.BOR, st, r_dest, rk_left, rk_right));
	}

	@Override
	public void visitBXOr(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		add(binaryOperation(LuaBinaryOperation.Op.BXOR, st, r_dest, rk_left, rk_right));
	}

	@Override
	public void visitShl(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		add(binaryOperation(LuaBinaryOperation.Op.SHL, st, r_dest, rk_left, rk_right));
	}

	@Override
	public void visitShr(Object id, SlotState st, int r_dest, int rk_left, int rk_right) {
		add(binaryOperation(LuaBinaryOperation.Op.SHR, st, r_dest, rk_left, rk_right));
	}

	@Override
	public void visitUnm(Object id, SlotState st, int r_dest, int r_arg) {
		switch (StaticMathImplementation.MAY_BE_INTEGER.opType(st.typeAt(r_arg))) {

			case Integer:
				add(e.loadRegister(r_arg, st, Number.class));
				add(BoxedPrimitivesMethods.unbox(Number.class, Type.LONG_TYPE));
				add(new InsnNode(LNEG));
				add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, Long.class));
				break;

			case Float:
				add(e.loadRegister(r_arg, st, Number.class));
				add(BoxedPrimitivesMethods.unbox(Number.class, Type.DOUBLE_TYPE));
				add(new InsnNode(DNEG));
				add(BoxedPrimitivesMethods.box(Type.DOUBLE_TYPE, Double.class));
				break;

			case Number:
				add(e.loadRegister(r_arg, st, Number.class));
				add(DispatchMethods.numeric(DispatchMethods.OP_UNM, 1));
				break;

			case Any:
				CodeEmitter.ResumptionPoint rp = e.resumptionPoint();
				add(rp.save());

				add(e.loadDispatchPreamble());
				add(e.loadRegister(r_arg, st));
				add(DispatchMethods.dynamic(DispatchMethods.OP_UNM, 1));

				add(rp.resume());
				add(e.retrieve_0());
				break;
		}

		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitBNot(Object id, SlotState st, int r_dest, int r_arg) {
		if (st.typeAt(r_arg).isSubtypeOf(LuaTypes.NUMBER_INTEGER)) {
			add(e.loadRegister(r_arg, st, Number.class));
			add(BoxedPrimitivesMethods.longValue(Number.class));
			add(ASMUtils.loadLong(-1L));
			add(new InsnNode(LXOR));
			add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, Long.class));
		}
		else {
			CodeEmitter.ResumptionPoint rp = e.resumptionPoint();
			add(rp.save());

			add(e.loadDispatchPreamble());
			add(e.loadRegister(r_arg, st));
			add(DispatchMethods.dynamic(DispatchMethods.OP_BNOT, 1));

			add(rp.resume());
			add(e.retrieve_0());
		}

		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitNot(Object id, SlotState st, int r_dest, int r_arg) {
		add(e.loadRegisterAsBoolean(r_arg, st));

		add(ASMUtils.loadInt(1));
		add(new InsnNode(IXOR));
		add(BoxedPrimitivesMethods.box(Type.BOOLEAN_TYPE, Type.getType(Boolean.class)));

		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitLen(Object id, SlotState st, int r_dest, int r_arg) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void visitConcat(Object id, SlotState st, int r_dest, int r_begin, int r_end) {
		throw new UnsupportedOperationException();  // TODO
	}

	protected InsnList dynamicComparison(String methodName, int rk_left, int rk_right, boolean pos, SlotState s, LabelNode trueBranch, LabelNode falseBranch) {
		InsnList il = new InsnList();

		CodeEmitter.ResumptionPoint rp = e.resumptionPoint();
		il.add(rp.save());

		il.add(e.loadDispatchPreamble());
		il.add(e.loadRegisterOrConstant(rk_left, s));
		il.add(e.loadRegisterOrConstant(rk_right, s));
		il.add(DispatchMethods.dynamic(methodName, 2));

		il.add(rp.resume());
		il.add(e.retrieve_0());

		// assuming that _0 is of type Boolean.class

		il.add(ASMUtils.checkCast(Boolean.class));
		il.add(BoxedPrimitivesMethods.booleanValue());

		// compare stack top with the expected value -- branch if not equal
		il.add(new JumpInsnNode(pos ? IFEQ : IFNE, falseBranch));

		// TODO: this could be a fall-through rather than a jump!
		il.add(new JumpInsnNode(GOTO, trueBranch));

		return il;
	}

	public InsnList cmp(String methodName, int rk_left, int rk_right, boolean pos, SlotState s, Object trueBranch, Object falseBranch) {
		InsnList il = new InsnList();

		// TODO: specialise
		il.add(dynamicComparison(methodName, rk_left, rk_right, pos, s, e._l(trueBranch), e._l(falseBranch)));

		return il;
	}

	@Override
	public void visitEq(Object id, SlotState st, boolean pos, int rk_left, int rk_right, Object trueBranchIdentity, Object falseBranchIdentity) {
		add(cmp(DispatchMethods.OP_EQ, rk_left, rk_right, pos, st, trueBranchIdentity, falseBranchIdentity));
	}

	@Override
	public void visitLe(Object id, SlotState st, boolean pos, int rk_left, int rk_right, Object trueBranchIdentity, Object falseBranchIdentity) {
		add(cmp(DispatchMethods.OP_LE, rk_left, rk_right, pos, st, trueBranchIdentity, falseBranchIdentity));
	}

	@Override
	public void visitLt(Object id, SlotState st, boolean pos, int rk_left, int rk_right, Object trueBranchIdentity, Object falseBranchIdentity) {
		add(cmp(DispatchMethods.OP_LT, rk_left, rk_right, pos, st, trueBranchIdentity, falseBranchIdentity));
	}

	@Override
	public void visitTest(Object id, SlotState st, int r_index, boolean value, Object trueBranchIdentity, Object falseBranchIdentity) {
		if (st.typeAt(r_index).isSubtypeOf(LuaTypes.BOOLEAN)) {
			add(e.loadRegister(r_index, st, Boolean.class));
			add(BoxedPrimitivesMethods.booleanValue());
		}
		else {
			add(e.loadRegister(r_index, st));
			add(UtilMethods.objectToBoolean());
		}

		LabelNode trueBranch = e._l(trueBranchIdentity);
		LabelNode falseBranch = e._l(falseBranchIdentity);

		// branch if non-equal
		add(new JumpInsnNode(value ? IFEQ : IFNE, falseBranch));
		add(new JumpInsnNode(GOTO, trueBranch));
	}

	@Override
	public void visitCall(Object id, SlotState st, int r_tgt, int b, int c) {
		CodeEmitter.ResumptionPoint rp = e.resumptionPoint();
		add(rp.save());

		int actualKind = InvokeKind.fromLua(b);

		add(e.loadDispatchPreamble());
		add(e.loadRegister(r_tgt, st));
		add(e.mapInvokeArgumentsToKinds(r_tgt + 1, st, b, actualKind));
		add(DispatchMethods.call(actualKind));

		add(rp.resume());

		if (c > 0) {
			st = st.consumeVarargs();
			add(e.retrieveAndStore(r_tgt, st, c - 1));
		}
		else {
			// keep results in the object sink
			// TODO: should all registers from r_tgt onwards be cleared?
		}
	}

	@Override
	public void visitTailCall(Object id, SlotState st, int r_tgt, int b) {
		if (b > 0) {
			// b - 1 is the actual number of arguments to the tailcall
			add(e.loadObjectSink());
			// FIXME: this needs to be remapped to an available invoke kind
			add(e.loadRegisters(r_tgt, st, b));  // target is at r_tgt, plus (b - 1) arguments
			add(ObjectSinkMethods.tailCall(b - 1));
			add(new InsnNode(RETURN));
		}
		else {
			add(e.setReturnValuesUpToStackTop(r_tgt, st));
			add(e.loadObjectSink());
			add(ObjectSinkMethods.markAsTailCall());
			add(new InsnNode(RETURN));
		}
	}

	@Override
	public void visitReturn(Object id, SlotState st, int r_from, int b) {
		if (b > 0) {
			// b - 1 is the actual number of results
			add(e.loadObjectSink());
			add(e.setReturnValuesFromRegisters(r_from, st, b - 1));
			add(new InsnNode(RETURN));
		}
		else {
			add(e.setReturnValuesUpToStackTop(r_from, st));
			add(new InsnNode(RETURN));
		}
	}

	@Override
	public void visitForLoop(Object id, SlotState st, int r_base, Object trueBranch, Object falseBranch) {

		// TODO: if we know the value of the step at compile time, we could avoid calling Dispatch.continueLoop().

		int r_index = r_base + 0;
		int r_limit = r_base + 1;
		int r_step = r_base + 2;

		LabelNode continueBranch = e._l(trueBranch);
		LabelNode breakBranch = e._l(falseBranch);
		
		net.sandius.rembulan.compiler.types.Type a0 = st.typeAt(r_index);  // index
		net.sandius.rembulan.compiler.types.Type a1 = st.typeAt(r_limit);  // limit
		net.sandius.rembulan.compiler.types.Type a2 = st.typeAt(r_step);  // step

		LuaInstruction.NumOpType loopType = LuaInstruction.NumOpType.loopType(a0, a1, a2);

		switch (loopType) {

			case Integer:
				// increment
				add(e.loadRegister(r_index, st, Number.class));
				add(BoxedPrimitivesMethods.longValue(Number.class));
				add(e.loadRegister(r_step, st, Number.class));
				add(BoxedPrimitivesMethods.longValue(Number.class));
				add(new InsnNode(LADD));
				add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, Type.getType(Long.class)));
				break;

			case Float:
				// increment index
				add(e.loadRegister(r_index, st, Number.class));
				add(BoxedPrimitivesMethods.doubleValue(Number.class));
				add(e.loadRegister(r_step, st, Number.class));
				add(BoxedPrimitivesMethods.doubleValue(Number.class));
				add(new InsnNode(DADD));
				add(BoxedPrimitivesMethods.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
				break;

			case Number:
				// increment index
				add(e.loadRegister(r_index, st, Number.class));
				add(e.loadRegister(r_step, st, Number.class));
				add(DispatchMethods.numeric(DispatchMethods.OP_ADD, 2));
				break;

			default:
				throw new IllegalStateException("Illegal loop type: " + loopType + " (base: " + r_index + "; slot state: " + st + ")");
		}

		// r_index is on stack
		add(new InsnNode(DUP));
		add(e.storeToRegister(r_index, st));  // save index into register

		add(e.loadRegister(r_limit, st, Number.class));
		add(e.loadRegister(r_step, st, Number.class));
		add(DispatchMethods.continueLoop());
		add(new JumpInsnNode(IFEQ, breakBranch));
		add(new JumpInsnNode(GOTO, continueBranch));
	}

	@Override
	public void visitForPrep(Object id, SlotState st, int r_base) {

		int r_index = r_base + 0;
		int r_limit = r_base + 1;
		int r_step = r_base + 2;
		
		LuaInstruction.NumOpType loopType = LuaInstruction.NumOpType.loopType(
				st.typeAt(r_index),
				st.typeAt(r_limit),
				st.typeAt(r_step));

		switch (loopType) {
			case Integer:
				// the initial decrement

				// convert to number if necessary
				if (!st.typeAt(r_limit).isSubtypeOf(LuaTypes.NUMBER)) {
					add(e.convertRegisterToNumber(r_limit, st, "'for' limit"));
				}

				add(e.loadRegister(r_index, st, Number.class));
				add(BoxedPrimitivesMethods.longValue(Number.class));
				add(e.loadRegister(r_step, st, Number.class));
				add(BoxedPrimitivesMethods.longValue(Number.class));
				add(new InsnNode(LSUB));
				add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, Type.getType(Long.class)));
				break;

			case Float:
				// convert to number if necessary
				if (!st.typeAt(r_limit).isSubtypeOf(LuaTypes.NUMBER)) {
					add(e.convertRegisterToNumber(r_limit, st, "'for' limit"));
				}

				// convert to float when necessary (we are in a float loop, so both of these parameters
				// are numbers, and at least one of them is a float)

				if (!st.typeAt(r_index).isSubtypeOf(LuaTypes.NUMBER_FLOAT)) {
					add(e.convertNumericRegisterToFloat(r_index, st));
				}
				if (!st.typeAt(r_step).isSubtypeOf(LuaTypes.NUMBER_FLOAT)) {
					add(e.convertNumericRegisterToFloat(r_step, st));
				}

				// the initial decrement
				add(e.loadRegister(r_index, st, Number.class));
				add(BoxedPrimitivesMethods.doubleValue(Number.class));
				add(e.loadRegister(r_step, st, Number.class));
				add(BoxedPrimitivesMethods.doubleValue(Number.class));
				add(new InsnNode(DSUB));
				add(BoxedPrimitivesMethods.box(Type.DOUBLE_TYPE, Type.getType(Double.class)));
				break;

			case Number:
				// We were unable to statically determine loop kind: force conversion of loop
				// parameters. Note that this does *not* imply that this is a float loop.

				// Note: we process parameters in the same order as in PUC Lua to get
				// the same error reporting.

				add(e.convertRegisterToNumber(r_limit, st, "'for' limit"));

				add(e.loadRegister(r_step, st));
				add(UtilMethods.objectToNumber("'for' step"));
				add(new InsnNode(DUP));
				add(e.storeToRegister(r_step, st));
				add(e.loadRegister(r_index, st));
				add(UtilMethods.objectToNumber("'for' initial value"));
				add(new InsnNode(SWAP));
				add(DispatchMethods.numeric(DispatchMethods.OP_SUB, 2));
				break;

			default:
				throw new IllegalStateException("Illegal loop type: " + loopType + " (base: " + r_index + "; slot state: " + st + ")");
		}

		add(e.storeToRegister(r_index, st));
	}

	@Override
	public void visitClosure(Object id, SlotState st, int r_dest, int index) {
		InsnList capture = new InsnList();
		InsnList load = new InsnList();
		int argCount = 0;

		for (Prototype.UpvalueDesc uvd : e.context().nestedPrototype(index).getUpValueDescriptions()) {
			if (uvd.inStack) {
				if (!st.isCaptured(uvd.index)) {
					capture.add(e.captureRegister(uvd.index));
					st = st.capture(uvd.index);  // just marking it so that we can store properly
				}
				load.add(e.loadRegisterValue(uvd.index, Upvalue.class));
			}
			else {
				load.add(e.getUpvalueReference(uvd.index));
			}

			argCount += 1;
		}

		String closureClassName = e.context().nestedPrototypeName(index);
		Type closureType = ASMUtils.typeForClassName(closureClassName);

		add(capture);
		add(new TypeInsnNode(NEW, closureType.getInternalName()));
		add(new InsnNode(DUP));
		add(load);
		add(ASMUtils.ctor(closureType, ASMUtils.fillTypes(Type.getType(Upvalue.class), argCount)));
		add(e.storeToRegister(r_dest, st));
	}

	@Override
	public void visitVararg(Object id, SlotState st, int r_base, int b) {
		if (b > 0) {
			// determinate case: (b - 1) is the number of varargs to load

			int n = b - 1;

			if (n > 0) {
				add(e.loadVarargs());
				for (int i = 0; i < n; i++) {
					if (i + 1 < n) {
						add(new InsnNode(DUP));
					}
					add(UtilMethods.getArrayElementOrNull(i));
					add(e.storeToRegister(r_base + i, st));
				}
			}
		}
		else {
			// indeterminate case
			add(e.loadObjectSink());
			add(e.loadVarargs());
			add(ObjectSinkMethods.setToArray());
		}
	}

}

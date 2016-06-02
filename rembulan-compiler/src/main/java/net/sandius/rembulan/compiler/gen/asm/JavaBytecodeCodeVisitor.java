package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.compiler.gen.CodeVisitor;
import net.sandius.rembulan.compiler.gen.LuaTypes;
import net.sandius.rembulan.compiler.gen.SlotState;
import net.sandius.rembulan.compiler.gen.block.LuaBinaryOperation;
import net.sandius.rembulan.compiler.gen.block.LuaInstruction;
import net.sandius.rembulan.compiler.gen.block.LuaUtils;
import net.sandius.rembulan.compiler.gen.block.StaticMathImplementation;
import net.sandius.rembulan.core.LFloat;
import net.sandius.rembulan.core.LInteger;
import net.sandius.rembulan.core.LNumber;
import net.sandius.rembulan.core.Table;
import net.sandius.rembulan.core.Upvalue;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntIterable;
import net.sandius.rembulan.util.IntIterator;
import net.sandius.rembulan.util.ReadOnlyArray;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

public class JavaBytecodeCodeVisitor extends CodeVisitor {

	// TODO: factor this outside make it a parameter
	public static final int FIELDS_PER_FLUSH = 50;

	private final RunMethodEmitter e;

	public JavaBytecodeCodeVisitor(RunMethodEmitter e) {
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
		RunMethodEmitter.ResumptionPoint rp = e.resumptionPoint();
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
		RunMethodEmitter.ResumptionPoint rp = e.resumptionPoint();
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
		RunMethodEmitter.ResumptionPoint rp = e.resumptionPoint();
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
		RunMethodEmitter.ResumptionPoint rp = e.resumptionPoint();
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
		RunMethodEmitter.ResumptionPoint rp = e.resumptionPoint();
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
			il.add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, Type.getType(LInteger.class)));
		}
		else {
			il.add(BoxedPrimitivesMethods.box(Type.DOUBLE_TYPE, Type.getType(LFloat.class)));
		}

		return il;
	}

	protected InsnList rawBinaryOperationAndBox(String name, boolean argsAreLong, boolean resultIsLong) {
		InsnList il = new InsnList();

		il.add(OperatorMethods.rawBinaryOperator(
				name,
				resultIsLong ? Type.LONG_TYPE : Type.DOUBLE_TYPE,
				argsAreLong ? Type.LONG_TYPE : Type.DOUBLE_TYPE));
		if (resultIsLong) {
			il.add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, Type.getType(LInteger.class)));
		}
		else {
			il.add(BoxedPrimitivesMethods.box(Type.DOUBLE_TYPE, Type.getType(LFloat.class)));
		}
		return il;
	}

	protected InsnList binaryIntegerOperation(LuaBinaryOperation.Op op, SlotState s, int rk_left, int rk_right) {
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
			case MOD:  il.add(rawBinaryOperationAndBox(OperatorMethods.RAW_OP_MOD, true, true)); break;
			case POW:  il.add(rawBinaryOperationAndBox(OperatorMethods.RAW_OP_POW, false, false)); break;
			case DIV:  il.add(nativeBinaryOperationAndBox(DDIV, false)); break;
			case IDIV: il.add(rawBinaryOperationAndBox(OperatorMethods.RAW_OP_IDIV, true, true)); break;
			case BAND: il.add(nativeBinaryOperationAndBox(LAND, true)); break;
			case BOR:  il.add(nativeBinaryOperationAndBox(LOR, true)); break;
			case BXOR: il.add(nativeBinaryOperationAndBox(LXOR, true)); break;
			case SHL:  il.add(nativeBinaryOperationAndBox(LSHL, true)); break;
			case SHR:  il.add(nativeBinaryOperationAndBox(LUSHR, true)); break;
			default: throw new IllegalStateException("Illegal binary integer op: " + op);
		}

		return il;
	}

	protected InsnList binaryFloatOperation(LuaBinaryOperation.Op op, SlotState s, int rk_left, int rk_right) {
		InsnList il = new InsnList();

		il.add(e.loadNumericRegisterOrConstantValue(rk_left, s, Type.DOUBLE_TYPE));
		il.add(e.loadNumericRegisterOrConstantValue(rk_right, s, Type.DOUBLE_TYPE));

		switch (op) {
			case ADD:  il.add(nativeBinaryOperationAndBox(DADD, false)); break;
			case SUB:  il.add(nativeBinaryOperationAndBox(DSUB, false)); break;
			case MUL:  il.add(nativeBinaryOperationAndBox(DMUL, false)); break;
			case MOD:  il.add(rawBinaryOperationAndBox(OperatorMethods.RAW_OP_MOD, false, false)); break;
			case POW:  il.add(rawBinaryOperationAndBox(OperatorMethods.RAW_OP_POW, false, false)); break;
			case DIV:  il.add(nativeBinaryOperationAndBox(DDIV, false)); break;
			case IDIV: il.add(rawBinaryOperationAndBox(OperatorMethods.RAW_OP_IDIV, false, false)); break;
			default: throw new IllegalStateException("Illegal binary float op: " + op);
		}

		return il;
	}

	protected InsnList binaryNumericOperation(LuaBinaryOperation.Op op, SlotState s, int rk_left, int rk_right) {
		InsnList il = new InsnList();

		il.add(e.loadRegisterOrConstant(rk_left, s, LNumber.class));
		il.add(e.loadRegisterOrConstant(rk_right, s, LNumber.class));
		il.add(DispatchMethods.numeric(DispatchMethods.binaryOperationMethodName(op), 2));

		return il;
	}

	protected InsnList binaryDynamicOperation(LuaBinaryOperation.Op op, SlotState s, int rk_left, int rk_right) {
		InsnList il = new InsnList();

		RunMethodEmitter.ResumptionPoint rp = e.resumptionPoint();
		il.add(rp.save());

		il.add(e.loadDispatchPreamble());
		il.add(e.loadRegisterOrConstant(rk_left, s));
		il.add(e.loadRegisterOrConstant(rk_right, s));
		il.add(DispatchMethods.dynamic(DispatchMethods.binaryOperationMethodName(op), 2));

		il.add(rp.resume());
		il.add(e.retrieve_0());

		return il;
	}

	protected InsnList binaryOperation(LuaBinaryOperation.Op op, SlotState s, int r_dest, int rk_left, int rk_right) {
		InsnList il = new InsnList();

		StaticMathImplementation staticMath = LuaBinaryOperation.mathForOp(op);
		LuaInstruction.NumOpType ot = staticMath.opType(
				LuaUtils.slotType(e.context(), s, rk_left),
				LuaUtils.slotType(e.context(), s, rk_right));

		switch (ot) {
			case Integer: il.add(binaryIntegerOperation(op, s, rk_left, rk_right)); break;
			case Float:   il.add(binaryFloatOperation(op, s, rk_left, rk_right)); break;
			case Number:  il.add(binaryNumericOperation(op, s, rk_left, rk_right)); break;
			case Any:     il.add(binaryDynamicOperation(op, s, rk_left, rk_right)); break;
		}

		il.add(e.storeToRegister(r_dest, s));

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

			// TODO: use LNumber methods instead of bytecode instructions

			case Integer:
				add(e.loadRegister(r_arg, st, LNumber.class));
				add(BoxedPrimitivesMethods.unbox(LNumber.class, Type.LONG_TYPE));
				add(new InsnNode(LNEG));
				add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, LInteger.class));
				break;

			case Float:
				add(e.loadRegister(r_arg, st, LNumber.class));
				add(BoxedPrimitivesMethods.unbox(LNumber.class, Type.DOUBLE_TYPE));
				add(new InsnNode(DNEG));
				add(BoxedPrimitivesMethods.box(Type.DOUBLE_TYPE, LFloat.class));
				break;

			case Number:
				add(e.loadRegister(r_arg, st, LNumber.class));
				add(DispatchMethods.numeric(DispatchMethods.OP_UNM, 1));
				break;

			case Any:
				RunMethodEmitter.ResumptionPoint rp = e.resumptionPoint();
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
			// TODO: call LNumber method
			add(e.loadRegister(r_arg, st, LNumber.class));
			add(BoxedPrimitivesMethods.longValue(LNumber.class));
			add(ASMUtils.loadLong(-1L));
			add(new InsnNode(LXOR));
			add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, LInteger.class));
		}
		else {
			RunMethodEmitter.ResumptionPoint rp = e.resumptionPoint();
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
		if (st.typeAt(r_arg).isSubtypeOf(LuaTypes.STRING)) {
			add(e.loadRegister(r_arg, st, String.class));
			add(OperatorMethods.stringLen());
			add(new InsnNode(I2L));
			add(BoxedPrimitivesMethods.box(Type.LONG_TYPE, LInteger.class));
		}
		else {
			RunMethodEmitter.ResumptionPoint rp = e.resumptionPoint();
			add(rp.save());

			add(e.loadDispatchPreamble());
			add(e.loadRegister(r_arg, st));
			add(DispatchMethods.dynamic(DispatchMethods.OP_LEN, 1));

			add(rp.resume());
			add(e.retrieve_0());
		}

		add(e.storeToRegister(r_dest, st));
	}

	private static boolean isStringable(SlotState st, int r) {
		return st.typeAt(r).isSubtypeOf(LuaTypes.STRING) || st.typeAt(r).isSubtypeOf(LuaTypes.NUMBER);
	}

	@Override
	public void visitConcat(Object id, SlotState st, int r_dest, int r_begin, int r_end) {

		// sanity check: we will be potentially overwriting all registers from r_begin to r_end,
		// assuming that they are not captured
		for (int i = r_begin; i <= r_end; i++) {
			Check.isFalse(st.isCaptured(i));
		}

		// CONCAT is right-associative, so we will be evaluating it right-to-left

		// find the rightmost non-stringable argument
		int r = r_end;
		while (r >= r_begin && isStringable(st, r)) {
			r--;
		}

		if (r + 1 < r_end) {
			// we have a (static) stringable suffix consisting of at least two elements

			add(new TypeInsnNode(NEW, Type.getInternalName(StringBuilder.class)));
			add(new InsnNode(DUP));
			add(ASMUtils.ctor(StringBuilder.class));

			for (int i = r + 1; i <= r_end; i++) {
				net.sandius.rembulan.compiler.types.Type t = st.typeAt(i);

				// register #i is stringable: a string or a number

				if (t.isSubtypeOf(LuaTypes.STRING)) {
					add(e.loadRegister(i, st, String.class));
				}
				else if (t.isSubtypeOf(LuaTypes.NUMBER)) {

					if (t.isSubtypeOf(LuaTypes.NUMBER_INTEGER)) {
						add(e.loadNumericRegisterOrConstantValue(i, st, Type.LONG_TYPE));
						add(OperatorMethods.unboxedNumberToLuaFormatString(Type.LONG_TYPE));
					}
					else if (t.isSubtypeOf(LuaTypes.NUMBER_FLOAT)) {
						add(e.loadNumericRegisterOrConstantValue(i, st, Type.DOUBLE_TYPE));
						add(OperatorMethods.unboxedNumberToLuaFormatString(Type.DOUBLE_TYPE));
					}
					else {
						add(e.loadRegister(i, st, LNumber.class));
						add(OperatorMethods.boxedNumberToLuaFormatString());
					}
				}
				else {
					throw new IllegalStateException("Unexpected type at register #" + i + ": " + t);
				}

				add(UtilMethods.StringBuilder_append(Type.getType(String.class)));

				// StringBuilder is now on stack top
			}

			add(UtilMethods.StringBuilder_toString());

			// if (r < r_begin), we don't have any dynamic prefix: save directly to r_dest;
			// save to the leftmost register of this suffix
			add(e.storeToRegister(r < r_begin ? r_dest : r + 1, st));
		}

		// dynamic / non-stringable prefix
		for ( ; r >= r_begin; r--) {

			if (r < r_end) {
				RunMethodEmitter.ResumptionPoint rp = e.resumptionPoint();
				add(rp.save());
				add(e.loadDispatchPreamble());
				add(e.loadRegister(r, st));
				add(e.loadRegister(r + 1, st));
				add(DispatchMethods.dynamic(DispatchMethods.OP_CONCAT, 2));

				add(rp.resume());
				add(e.retrieve_0());

				// save the last result to r_dest, otherwise keep rewriting the already used registers
				add(e.storeToRegister(r == r_begin ? r_dest : r, st));
			}
		}

	}

	private int cmpBranchOpcode(LuaInstruction.Comparison op, boolean pos) {
		switch (op) {
			case EQ:  return pos ? IFNE : IFEQ;
			case LT:  return pos ? IFGE : IFLT;
			case LE:  return pos ? IFGT : IFLE;
			default:  throw new UnsupportedOperationException("Illegal comparison operation: " + op);
		}
	}

	protected InsnList integerComparison(LuaInstruction.Comparison op, int rk_left, int rk_right, boolean pos, SlotState s, LabelNode falseBranch) {
		InsnList il = new InsnList();

		il.add(e.loadNumericRegisterOrConstantValue(rk_left, s, Type.LONG_TYPE));
		il.add(e.loadNumericRegisterOrConstantValue(rk_right, s, Type.LONG_TYPE));
		il.add(new InsnNode(LCMP));

		il.add(new JumpInsnNode(cmpBranchOpcode(op, pos), falseBranch));

		return il;
	}

	protected InsnList floatComparison(LuaInstruction.Comparison op, int rk_left, int rk_right, boolean pos, SlotState s, LabelNode falseBranch) {
		InsnList il = new InsnList();

		il.add(e.loadNumericRegisterOrConstantValue(rk_left, s, Type.DOUBLE_TYPE));
		il.add(e.loadNumericRegisterOrConstantValue(rk_right, s, Type.DOUBLE_TYPE));

		// Let OP be one of {EQ, LT, LE}. If we expect that
		//   rk[rk_left] OP rk[rk_right] == true,
		// then it is safe to treat comparisons involving NaNs as +1, since we're always
		// interested in the lesser-than relation. Conversely, if the expectation == false,
		// it is safe to treat NaNs as -1.
		il.add(new InsnNode(pos ? DCMPG : DCMPL));

		il.add(new JumpInsnNode(cmpBranchOpcode(op, pos), falseBranch));

		return il;
	}

	protected InsnList numericComparison(LuaInstruction.Comparison op, int rk_left, int rk_right, boolean pos, SlotState s, LabelNode falseBranch) {
		InsnList il = new InsnList();

		il.add(e.loadRegisterOrConstant(rk_left, s, LNumber.class));
		il.add(e.loadRegisterOrConstant(rk_right, s, LNumber.class));
		il.add(DispatchMethods.numeric(DispatchMethods.comparisonMethodName(op), 2));

		il.add(new JumpInsnNode(pos ? IFEQ : IFNE, falseBranch));

		return il;
	}

	protected InsnList stringComparison(LuaInstruction.Comparison op, int rk_left, int rk_right, boolean pos, SlotState s, LabelNode falseBranch) {
		InsnList il = new InsnList();

		il.add(e.loadRegisterOrConstant(rk_left, s, String.class));
		il.add(e.loadRegisterOrConstant(rk_right, s, String.class));
		il.add(UtilMethods.String_compareTo());

		il.add(new JumpInsnNode(cmpBranchOpcode(op, pos), falseBranch));

		return il;
	}

	protected InsnList dynamicComparison(LuaInstruction.Comparison op, int rk_left, int rk_right, boolean pos, SlotState s, LabelNode falseBranch) {
		InsnList il = new InsnList();

		RunMethodEmitter.ResumptionPoint rp = e.resumptionPoint();
		il.add(rp.save());

		il.add(e.loadDispatchPreamble());
		il.add(e.loadRegisterOrConstant(rk_left, s));
		il.add(e.loadRegisterOrConstant(rk_right, s));
		il.add(DispatchMethods.dynamic(DispatchMethods.comparisonMethodName(op), 2));

		il.add(rp.resume());
		il.add(e.retrieve_0());

		// assuming that _0 is of type Boolean.class

		il.add(ASMUtils.checkCast(Boolean.class));
		il.add(BoxedPrimitivesMethods.booleanValue());

		// compare stack top with the expected value -- branch if not equal
		il.add(new JumpInsnNode(pos ? IFEQ : IFNE, falseBranch));

		return il;
	}

	public InsnList cmp(LuaInstruction.Comparison op, int rk_left, int rk_right, boolean pos, SlotState s, Object trueBranchIdentity, Object falseBranchIdentity) {
		InsnList il = new InsnList();

		LuaInstruction.ComparisonOpType cmpOpType = LuaInstruction.ComparisonOpType.forTypes(
				LuaUtils.slotType(e.context(), s, rk_left),
				LuaUtils.slotType(e.context(), s, rk_right));

		LabelNode falseBranch = e._l(falseBranchIdentity);

		switch (cmpOpType) {
			case Integer: il.add(integerComparison(op, rk_left, rk_right, pos, s, falseBranch)); break;
			case Float:   il.add(floatComparison(op, rk_left, rk_right, pos, s, falseBranch)); break;
			case Numeric: il.add(numericComparison(op, rk_left, rk_right, pos, s, falseBranch)); break;
			case String:  il.add(stringComparison(op, rk_left, rk_right, pos, s, falseBranch)); break;
			case Dynamic: il.add(dynamicComparison(op, rk_left, rk_right, pos, s, falseBranch)); break;
			default: throw new IllegalArgumentException("Illegal comparison operation: " + op);
		}

		// TODO: this could be a fall-through rather than a jump!
		il.add(new JumpInsnNode(GOTO, e._l(trueBranchIdentity)));

		return il;
	}

	@Override
	public void visitEq(Object id, SlotState st, boolean pos, int rk_left, int rk_right, Object trueBranchIdentity, Object falseBranchIdentity) {
		add(cmp(LuaInstruction.Comparison.EQ, rk_left, rk_right, pos, st, trueBranchIdentity, falseBranchIdentity));
	}

	@Override
	public void visitLe(Object id, SlotState st, boolean pos, int rk_left, int rk_right, Object trueBranchIdentity, Object falseBranchIdentity) {
		add(cmp(LuaInstruction.Comparison.LE, rk_left, rk_right, pos, st, trueBranchIdentity, falseBranchIdentity));
	}

	@Override
	public void visitLt(Object id, SlotState st, boolean pos, int rk_left, int rk_right, Object trueBranchIdentity, Object falseBranchIdentity) {
		add(cmp(LuaInstruction.Comparison.LT, rk_left, rk_right, pos, st, trueBranchIdentity, falseBranchIdentity));
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
		RunMethodEmitter.ResumptionPoint rp = e.resumptionPoint();
		add(rp.save());

		int kind = DispatchMethods.adjustKind_call(b);

		add(e.loadDispatchPreamble());
		add(e.loadRegister(r_tgt, st));
		add(e.mapInvokeArgumentsToKinds(r_tgt + 1, st, b, kind));
		add(DispatchMethods.call(kind));

		add(rp.resume());

		if (c > 0) {
			st = st.consumeVarargs();
			add(e.retrieveAndStore(r_tgt, st, c - 1));
		}
		else {
			// keep results in the object sink, but clear registers from r_tgt onwards
			add(e.clearRegisters(r_tgt));
		}
	}

	@Override
	public void visitTailCall(Object id, SlotState st, int r_tgt, int b) {
		int kind = ObjectSinkMethods.adjustKind_tailCall(b);

		add(e.loadObjectSink());
		add(e.loadRegister(r_tgt, st));
		add(e.mapInvokeArgumentsToKinds(r_tgt + 1, st, b, kind));
		add(ObjectSinkMethods.tailCall(kind));
		add(new InsnNode(ACONST_NULL));
		add(new InsnNode(ARETURN));
	}

	@Override
	public void visitReturn(Object id, SlotState st, int r_from, int b) {
		int kind = ObjectSinkMethods.adjustKind_setTo(b);

		add(e.loadObjectSink());
		add(e.mapInvokeArgumentsToKinds(r_from, st, b, kind));
		add(ObjectSinkMethods.setTo(kind));
		add(new InsnNode(ACONST_NULL));
		add(new InsnNode(ARETURN));
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

		// increment index
		switch (loopType) {
			case Integer: add(binaryIntegerOperation(LuaBinaryOperation.Op.ADD, st, r_index, r_step)); break;
			case Float:   add(binaryFloatOperation(LuaBinaryOperation.Op.ADD, st, r_index, r_step)); break;
			case Number:  add(binaryNumericOperation(LuaBinaryOperation.Op.ADD, st, r_index, r_step)); break;

			default: throw new IllegalStateException("Illegal loop type: " + loopType + " (base: " + r_index + "; slot state: " + st + ")");
		}

		// r_index is on stack
		add(new InsnNode(DUP));
		add(e.storeToRegister(r_index, st));  // save index into register

		add(e.loadRegister(r_limit, st, LNumber.class));
		add(e.loadRegister(r_step, st, LNumber.class));
		add(DispatchMethods.continueLoop());
		add(new JumpInsnNode(IFEQ, breakBranch));
		add(new JumpInsnNode(GOTO, continueBranch));
	}

	@Override
	public void visitForPrep(Object id, SlotState st, int r_base) {

		int r_index = r_base + 0;
		int r_limit = r_base + 1;
		int r_step = r_base + 2;
		
		LuaInstruction.NumOpType loopType = StaticMathImplementation.MAY_BE_INTEGER.opType(
				st.typeAt(r_index),
				st.typeAt(r_step));

		// Note: we coerce parameters to numbers in the same order as in PUC Lua to get
		// the same error reporting.

		// convert loop limit to number if necessary
		if (!st.typeAt(r_limit).isSubtypeOf(LuaTypes.NUMBER)) {
			add(e.convertRegisterToNumber(r_limit, st, "'for' limit"));
		}

		switch (loopType) {
			case Integer:
				add(binaryIntegerOperation(LuaBinaryOperation.Op.SUB, st, r_index, r_step));
				break;

			case Float:
				if (!st.typeAt(r_step).isSubtypeOf(LuaTypes.NUMBER_FLOAT)) {
					add(e.convertNumericRegisterToFloat(r_step, st));
				}
				add(binaryFloatOperation(LuaBinaryOperation.Op.SUB, st, r_index, r_step));
				break;

			case Any:
				if (!st.typeAt(r_step).isSubtypeOf(LuaTypes.NUMBER)) {
					add(e.convertRegisterToNumber(r_step, st, "'for' step"));
				}
				if (!st.typeAt(r_index).isSubtypeOf(LuaTypes.NUMBER)) {
					add(e.convertRegisterToNumber(r_index, st, "'for' initial value"));
				}
				// fall through to the Number case

			case Number:
				add(binaryNumericOperation(LuaBinaryOperation.Op.SUB, st, r_index, r_step));
				break;

			default:
				throw new IllegalStateException("Illegal loop type: " + loopType + " (base: " + r_index + "; slot state: " + st + ")");
		}

		add(e.storeToRegister(r_index, st));
	}

	@Override
	public void visitTForCall(Object id, SlotState st, int r_base, int c) {
		RunMethodEmitter.ResumptionPoint rp = e.resumptionPoint();
		add(rp.save());

		int kind = DispatchMethods.adjustKind_call(3);
		add(e.loadDispatchPreamble());
		add(e.loadRegister(r_base, st));
		add(e.mapInvokeArgumentsToKinds(r_base + 1, st, 3, kind));
		add(DispatchMethods.call(kind));

		add(rp.resume());
		for (int i = 0; i < c; i++) {
			add(e.loadObjectSink());
			add(ObjectSinkMethods.get(i));
			add(e.storeToRegister(r_base + 3 + i, st));
		}
	}

	@Override
	public void visitTForLoop(Object id, SlotState st, int r_base, Object trueBranchIdentity, Object falseBranchIdentity) {
		add(e.loadRegister(r_base + 1, st));
		add(new JumpInsnNode(IFNONNULL, e._l(trueBranchIdentity)));
		add(new JumpInsnNode(GOTO, e._l(falseBranchIdentity)));
	}

	@Override
	public void visitSetList(Object id, SlotState st, int r_base, int b, int c) {

		// We require that the destination object is a table: furthermore,
		// we assume that the table has just been constructed by a NEWTABLE
		// instruction, and therefore cannot have a metatable. All accesses
		// may therefore be raw.

		Check.isTrue(st.typeAt(r_base).isSubtypeOf(LuaTypes.TABLE));

		int offset = (c - 1) * FIELDS_PER_FLUSH;

		if (b > 0) {
			add(e.loadRegister(r_base, st, Table.class));

			for (int i = 1; i <= b; i++) {
				if (i < b) {
					add(new InsnNode(DUP));
				}

				add(ASMUtils.loadInt(offset + i));
				add(e.loadRegister(r_base + i, st));
				add(OperatorMethods.tableRawSetIntKey());
			}
		}
		else {
			Check.isTrue(st.hasVarargs());

			int vp = st.varargPosition() - r_base;

			Check.nonNegative(vp);

			add(e.loadRegister(r_base, st, Table.class));

			// we're copying from (r_base + 1) (inclusive) to vp (exclusive)
			// -- these indices are in the locals

			for (int i = 1; i < vp; i++) {
				add(new InsnNode(DUP));
				add(ASMUtils.loadInt(offset + i));
				add(e.loadRegister(r_base + i, st));
				add(OperatorMethods.tableRawSetIntKey());
			}

			// last used index is (offset + vp - 1), so the next one is (offset + vp)
			int varOffset = offset + vp;

			LabelNode tabBegin = new LabelNode();
			LabelNode iterBegin = new LabelNode();
			LabelNode iterEnd = new LabelNode();

			int lv_tab = e.newLocalVariable(0, "t", tabBegin, iterEnd, Type.getType(Table.class));
			int lv_idx = e.newLocalVariable(1, "i", iterBegin, iterEnd, Type.INT_TYPE);

			// table is still on stack: store it into a local variable
			add(new VarInsnNode(ASTORE, lv_tab));
			add(tabBegin);

			// now implement a for(int i = 0; i < sink.size(); i++) { ... }
			// TODO: store the limit in a local variable? no need to query for it in each iteration

			add(ASMUtils.loadInt(0));
			add(new VarInsnNode(ISTORE, lv_idx));

			add(iterBegin);
			add(new FrameNode(F_APPEND, 2, new Object[] { Type.getInternalName(Table.class), Opcodes.INTEGER }, 0, null));
			add(new VarInsnNode(ILOAD, lv_idx));
			add(e.loadObjectSink());
			add(ObjectSinkMethods.size());
			add(new JumpInsnNode(IF_ICMPGE, iterEnd));

			// load the table
			add(new VarInsnNode(ALOAD, lv_tab));

			// determine the table index for this element: it's (i + varOffset)
			add(new VarInsnNode(ILOAD, lv_idx));
			if (varOffset > 0) {
				add(ASMUtils.loadInt(varOffset));
				add(new InsnNode(IADD));
			}

			// load i-th value from sink
			add(e.loadObjectSink());
			add(new VarInsnNode(ILOAD, lv_idx));
			add(ObjectSinkMethods.get());

			// save to table
			add(OperatorMethods.tableRawSetIntKey());

			// increment index & iterate
			add(new IincInsnNode(lv_idx, 1));
			add(new JumpInsnNode(GOTO, iterBegin));

			add(iterEnd);
			add(new FrameNode(F_CHOP, 2, null, 0, null));
		}
	}

	@Override
	public void visitClosure(Object id, SlotState st, int r_dest, int index) {
		ReadOnlyArray<Prototype.UpvalueDesc> upvalues = e.context().nestedPrototype(index).getUpValueDescriptions();
		String closureClassName = e.context().nestedPrototypeName(index);
		Type closureType = ASMUtils.typeForClassName(closureClassName);

		ClassEmitter.NestedInstanceKind kind = e.nestedClosureKind(index);

		switch (kind) {
			case Pure:
				// load the static instance
				add(new FieldInsnNode(
						GETSTATIC,
						closureType.getInternalName(),
						ClassEmitter.instanceFieldName(),
						closureType.getDescriptor()));
				break;

			case Closed:
				// load the field
				add(new VarInsnNode(ALOAD, 0));
				add(e.getNestedInstanceField(index));
				break;

			case Open:
				InsnList capture = new InsnList();
				InsnList load = new InsnList();
				int argCount = 0;

				for (Prototype.UpvalueDesc uvd : upvalues) {
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

				add(capture);

				add(new TypeInsnNode(NEW, closureType.getInternalName()));
				add(new InsnNode(DUP));
				add(load);
				add(ASMUtils.ctor(closureType, ASMUtils.fillTypes(Type.getType(Upvalue.class), argCount)));
				break;

			default:
				throw new UnsupportedOperationException("Illegal nested instance kind: " + kind);
		}

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
			add(ObjectSinkMethods.setTo(0));
		}
	}

	@Override
	public void visitCpuCheck(int cost) {
		add(e.cpuCheck(cost));
	}

}

package net.sandius.rembulan.gen;

import net.sandius.rembulan.core.CallInfo;
import net.sandius.rembulan.core.ControlThrowable;
import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.core.LuaState;
import net.sandius.rembulan.core.OpCode;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.Preempted;
import net.sandius.rembulan.core.Prototype;
import net.sandius.rembulan.core.PrototypeToClassMap;
import net.sandius.rembulan.core.Registers;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;
import net.sandius.rembulan.util.asm.ASMUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class LuaBytecodeMethodVisitor extends MethodVisitor implements InstructionEmitter {

	private static Type REGISTERS_TYPE = ASMUtils.arrayTypeFor(Object.class);
	private static final int REGISTER_OFFSET = 4;

	private static final int LVAR_THIS = 0;
	private static final int LVAR_SELF = 1;
	private static final int LVAR_RET = 2;
	private static final int LVAR_PC = 3;

	private final Type thisType;
	private final ReadOnlyArray<Object> constants;
	private final ReadOnlyArray<Prototype> nestedPrototypes;
	private final PrototypeToClassMap prototypeToClassMap;

	private final int numRegs;

	private Label l_first;
	private Label l_last;
	private Label l_default;
	private Label l_save_and_yield;

	private final int numInstrs;
	private Label[] l_pc_begin;
	private Label[] l_pc_end;
	private Label[] l_pc_preempt_handler;

	protected InstructionEmitter ie;

	public static void emitConstructor(ClassVisitor cv, Type thisType) {
		Type ctorType = Type.getMethodType(
				Type.VOID_TYPE
		);

		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", ctorType.getDescriptor(), null, null);
		mv.visitCode();
		Label l_begin = new Label();
		mv.visitLabel(l_begin);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Function.class), "<init>", ctorType.getDescriptor(), false);
		mv.visitInsn(RETURN);
		Label l_end = new Label();
		mv.visitLabel(l_end);
		mv.visitLocalVariable("this", thisType.getDescriptor(), null, l_begin, l_end, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private final static String RESUME_METHOD_NAME = "run";

	private final static Type RESUME_METHOD_TYPE = Type.getMethodType(
			Type.VOID_TYPE,
			Type.getType(Registers.class),
			Type.getType(Registers.class),
			Type.INT_TYPE
	);

	private final static String CALL_METHOD_NAME = "call";

	private final static Type CALL_METHOD_TYPE = Type.getMethodType(
			Type.VOID_TYPE,
			Type.getType(Registers.class),
			Type.getType(Registers.class)
	);

	public LuaBytecodeMethodVisitor(ClassVisitor cv, Type thisType, ReadOnlyArray<Object> constants, ReadOnlyArray<Prototype> nestedPrototypes, PrototypeToClassMap prototypeToClassMap, int numInstrs, int numRegs) {
		super(ASM5);
		Check.notNull(cv);
		Check.notNull(thisType);

		this.thisType = thisType;
		this.constants = constants;
		this.nestedPrototypes = nestedPrototypes;
		this.prototypeToClassMap = prototypeToClassMap;
		this.numRegs = numRegs;
		this.numInstrs = numInstrs;

		ie = this;

		l_first = new Label();
		l_last = new Label();
		l_save_and_yield = new Label();
		l_default = new Label();

		// luapc-to-jvmpc mapping
		l_pc_begin = new Label[numInstrs];
		l_pc_end = new Label[numInstrs];
		l_pc_preempt_handler = new Label[numInstrs];
		for (int i = 0; i < l_pc_begin.length; i++) {
			l_pc_begin[i] = new Label();
			l_pc_end[i] = new Label();
			l_pc_preempt_handler[i] = new Label();
		}

		mv = cv.visitMethod(ACC_PROTECTED, RESUME_METHOD_NAME, RESUME_METHOD_TYPE.getDescriptor(),
				null,
				new String[] { Type.getInternalName(ControlThrowable.class) });
	}

	public void begin() {
		visitParameter("self", 0);
		visitParameter("ret", 0);
		visitParameter("pc", 0);

		visitCode();
		luaCodeBegin();
	}

	public void end() {
		luaCodeEnd();

		mv.visitLabel(l_last);

		visitLocalVariable("this", thisType.getDescriptor(), null, l_first, l_last, LVAR_THIS);
		visitLocalVariable("self", Type.getDescriptor(Registers.class), null, l_first, l_last, LVAR_SELF);
		visitLocalVariable("ret", Type.getDescriptor(Registers.class), null, l_first, l_last, LVAR_SELF);
		visitLocalVariable("pc", Type.INT_TYPE.getDescriptor(), null, l_first, l_last, LVAR_PC);

		// registers
		for (int i = 0; i < numRegs; i++) {
			visitLocalVariable("r_" + (i + 1), Type.getDescriptor(Object.class), null, l_first, l_last, REGISTER_OFFSET + i);
		}

		visitMaxs(numRegs + 6, REGISTER_OFFSET + numRegs);

		visitEnd();

		System.err.println("Begin: " + l_first.getOffset());
		System.err.println("Save-and-yield: " + l_save_and_yield.getOffset());
		System.err.println("Error branch: " + l_default.getOffset());
		System.err.println("End: " + l_last.getOffset());

		for (int i = 0; i < numInstrs; i++) {
			System.err.println("Handler for pc=" + i + ": [" + l_pc_begin[i].getOffset() + ", " + l_pc_end[i].getOffset() + ") -> " + l_pc_preempt_handler[i].getOffset());
		}

	}

	public void luaCodeBegin() {
		preamble();
	}

	public void luaCodeEnd() {
		visitLabel(l_pc_end[numInstrs - 1]);

//		visitInsn(NOP);

		emitPreemptHandlers();
		emitSaveRegistersAndThrowBranch();

		emitErrorBranch();
	}

	// save registers and yield
	// pc must be saved by now
	// control throwable is on the stack top
	public void emitSaveRegistersAndThrowBranch() {
		visitLabel(l_save_and_yield);
		visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{Type.getInternalName(ControlThrowable.class)});
		saveAllRegistersToBase();
		pushCallInfoAndThrow();
	}

	private void constructCallInfo() {
		visitTypeInsn(NEW, Type.getInternalName(CallInfo.class));
		visitInsn(DUP);
		pushThis();
		pushSelf();
		pushRet();
		loadPc();
		visitMethodInsn(INVOKESPECIAL, Type.getInternalName(CallInfo.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Function.class), Type.getType(Registers.class), Type.getType(Registers.class), Type.INT_TYPE), false);
	}

	private void pushCallInfoAndThrow() {
		// control throwable is on the stack top
		visitInsn(DUP);
		constructCallInfo();
		visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(ControlThrowable.class), "push", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(CallInfo.class)), false);
		visitInsn(ATHROW);
	}

	// error branch
	public void emitErrorBranch() {
		visitLabel(l_default);
		visitLineNumber(2, l_default);
		visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		visitTypeInsn(NEW, Type.getInternalName(IllegalStateException.class));  // TODO: use a more precise exception
		visitInsn(DUP);
		visitMethodInsn(INVOKESPECIAL, Type.getInternalName(IllegalStateException.class), "<init>", "()V", false);
		visitInsn(ATHROW);
	}


	public void preemptHandler(int pc) {
		visitLabel(l_pc_preempt_handler[pc]);
		visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { Type.getInternalName(ControlThrowable.class) });
		savePc(pc);
		visitJumpInsn(GOTO, l_save_and_yield);
	}

	public void declarePreemptHandler(int pc) {
		// FIXME: continuing with pc + 1 is a hack!
		visitTryCatchBlock(l_pc_begin[pc], l_pc_end[pc], l_pc_preempt_handler[pc + 1], Type.getInternalName(ControlThrowable.class));
	}

	public void declarePreemptHandlers() {
		for (int i = 0; i < numInstrs - 1; i++) {
			declarePreemptHandler(i);
		}
	}

	public void emitPreemptHandlers() {
		for (int i = 0; i < numInstrs; i++) {
			preemptHandler(i);
		}
	}

	public void preamble() {
		declarePreemptHandlers();

		visitLabel(l_first);
		visitLineNumber(2, l_first);

		loadRegisters();

		Object[] regTypes = new Object[numRegs];
		for (int i = 0; i < regTypes.length; i++) {
			regTypes[i] = Type.getInternalName(Object.class);
		}

		visitFrame(Opcodes.F_APPEND, numRegs, regTypes, 0, null);

		// branch according to the program counter
		preambleSwitch();
	}

	private void preambleSwitch() {
		loadPc();
		mv.visitTableSwitchInsn(0, numInstrs - 1, l_default, l_pc_begin);
	}

	private void pushThis() {
		visitVarInsn(ALOAD, LVAR_THIS);
	}

	private void pushSelf() {
		visitVarInsn(ALOAD, LVAR_SELF);
	}

	private void pushRet() {
		visitVarInsn(ALOAD, LVAR_RET);
	}

	private void pushRegister(int idx) {
		visitVarInsn(ALOAD, REGISTER_OFFSET + idx);
	}

	private void pushIntoRegister(int idx) {
		visitVarInsn(ASTORE, REGISTER_OFFSET + idx);
	}

	private void pushInt(int i) {
		if (i >= -1 && i <= 5) mv.visitInsn(ICONST_0 + i);
		else mv.visitLdcInsn(i);
	}

	private void pushLong(long l) {
		if (l >= 0 && l <= 1) mv.visitInsn(LCONST_0 + (int) l);
		else mv.visitLdcInsn(l);
	}

	private void pushFloat(float f) {
		if (f == 0.0f) mv.visitInsn(FCONST_0);
		else if (f == 1.0f) mv.visitInsn(FCONST_1);
		else if (f == 2.0f) mv.visitInsn(FCONST_2);
		else mv.visitLdcInsn(f);
	}

	public void pushDouble(double d) {
		if (d == 0.0) mv.visitInsn(DCONST_0);
		else if (d == 1.0) mv.visitInsn(DCONST_1);
		else mv.visitLdcInsn(d);
	}

	public void pushString(String s) {
		Check.notNull(s);
		mv.visitLdcInsn(s);
	}

	private void pushPlus(int offset) {
		if (offset > 0) {
			pushInt(offset);
			mv.visitInsn(IADD);
		}
	}

//	private void pushBasePlus(int offset) {
////		pushThis();
//		visitVarInsn(ILOAD, LVAR_BASE);
//		pushPlus(offset);
//	}
//
//	private void pushReturnBasePlus(int offset) {
////		pushThis();
//		visitVarInsn(ILOAD, LVAR_RETURN_BASE);
//		pushPlus(offset);
//	}

//	private void pushObjectStack() {
//		pushCoroutine();
//		visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Coroutine.class), "getObjectStack", Type.getMethodDescriptor(Type.getType(ObjectStack.class)), false);
//	}

	private void loadRegister(int idx) {
		Check.nonNegative(idx);

		pushSelf();
		pushInt(idx);
		visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Registers.class), "get", Type.getMethodDescriptor(Type.getType(Object.class), Type.INT_TYPE), true);
		visitVarInsn(ASTORE, REGISTER_OFFSET + idx);
	}

	private void saveRegisterToBase(int idx) {
		Check.nonNegative(idx);

		pushSelf();
		pushInt(idx);
		pushRegister(idx);
		visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Registers.class), "set", Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.getType(Object.class)), true);
	}

	private void saveRegisterToRet(int idx) {
		Check.nonNegative(idx);

		pushRet();
		pushInt(idx);
		pushRegister(idx);
		visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Registers.class), "set", Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.getType(Object.class)), true);
	}

	public void loadRegisters() {
		// load registers into local variables
		for (int i = 0; i < numRegs; i++) {
			loadRegister(i);
		}
	}

	@Deprecated
	public void saveAllRegistersToBase() {
		saveRegistersToBase(0, numRegs - 1);
	}

	public void saveRegistersToBase(int from, int to) {
		for (int i = from; i <= to; i++) {
			saveRegisterToBase(i);
		}
	}

	public void saveRegistersToRet(int from, int to) {
		for (int i = from; i <= to; i++) {
			saveRegisterToRet(i);
		}
	}

	public void loadPc() {
		visitVarInsn(ILOAD, LVAR_PC);
	}

	public void savePc(int pc) {
		pushInt(pc);
		visitVarInsn(ISTORE, LVAR_PC);
	}

	public void setTop(int to) {
		pushSelf();
		pushInt(to);
		mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Registers.class), "setTop", Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE), true);
	}

	private void checkPreemptFromHere(int pc) {
		// must be here... at least in case the instruction is return
		visitFrame(Opcodes.F_SAME, 0, null, 0, null);

		visitMethodInsn(INVOKESTATIC, Type.getInternalName(LuaState.class), "getCurrentState", Type.getMethodDescriptor(Type.getType(LuaState.class)), false);
		visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(LuaState.class), "shouldPreemptNow", Type.getMethodDescriptor(Type.BOOLEAN_TYPE), false);

		mv.visitJumpInsn(IFEQ, l_pc_begin[pc]);  // continue with pc + 1

		pushPreemptThrowable();
		visitJumpInsn(GOTO, l_pc_preempt_handler[pc]);
	}


	public void atPc(int pc, int lineNumber) {
		if (pc > 0) {
			mv.visitLabel(l_pc_end[pc - 1]);
			checkPreemptFromHere(pc);
		}

//		declarePreemptHandler(pc);

		mv.visitLabel(l_pc_begin[pc]);
		if (lineNumber > 0) mv.visitLineNumber(lineNumber, l_pc_begin[pc]);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
	}

	public void pushPreemptThrowable() {
		visitMethodInsn(INVOKESTATIC, Type.getInternalName(Preempted.class), "newInstance", Type.getMethodDescriptor(Type.getType(Preempted.class)), false);
	}

//	public void rethrow() {
//		mv.visitInsn(ATHROW);
//	}

//	public void preempted() {
//		pushPreemptThrowable();
//		rethrow();
//	}

	public void pushConstant(int idx) {
		Object c = constants.get(idx);

		if (c instanceof Integer) {
			pushInt((Integer) c);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
		}
		else if (c instanceof Long) {
			pushLong((Long) c);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
		}
		else if (c instanceof Float) {
			pushFloat((Float) c);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
		}
		else if (c instanceof Double) {
			pushDouble((Double) c);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
		}
		else if (c instanceof String) {
			pushString((String) c);
		}
		else {
			throw new IllegalArgumentException("Unsupported constant type: " + c.getClass());
		}
	}

	public void instruction(int i) {
		int oc = OpCode.opCode(i);

		int a = OpCode.arg_A(i);
		int b = OpCode.arg_B(i);
		int c = OpCode.arg_C(i);
		int ax = OpCode.arg_Ax(i);
		int bx = OpCode.arg_Bx(i);
		int sbx = OpCode.arg_sBx(i);

		switch (oc) {

			case OpCode.MOVE:     ie.l_MOVE(a, b); break;
			case OpCode.LOADK:    ie.l_LOADK(a, bx); break;
			//case OpCode.LOADKX:   ie.l_LOADKX(extra);  break;
			case OpCode.LOADBOOL: ie.l_LOADBOOL(a, b, c); break;
			case OpCode.LOADNIL:  ie.l_LOADNIL(a, b); break;
			case OpCode.GETUPVAL: ie.l_GETUPVAL(a, b); break;
			case OpCode.GETTABUP: ie.l_GETTABUP(a, b, c); break;
			case OpCode.GETTABLE: ie.l_GETTABLE(a, b, c); break;
			case OpCode.SETTABUP: ie.l_SETTABUP(a, b, c); break;
			case OpCode.SETUPVAL: ie.l_SETUPVAL(a, b); break;
			case OpCode.SETTABLE: ie.l_SETTABLE(a, b, c); break;
			case OpCode.NEWTABLE: ie.l_NEWTABLE(a, b, c); break;
			case OpCode.SELF:     ie.l_SELF(a, b, c); break;
			
			case OpCode.ADD:   ie.l_ADD(a, b, c); break;
			case OpCode.SUB:   ie.l_SUB(a, b, c); break;
			case OpCode.MUL:   ie.l_MUL(a, b, c); break;
			case OpCode.MOD:   ie.l_MOD(a, b, c); break;
			case OpCode.POW:   ie.l_POW(a, b, c); break;
			case OpCode.DIV:   ie.l_DIV(a, b, c); break;
			case OpCode.IDIV:  ie.l_IDIV(a, b, c); break;
			case OpCode.BAND:  ie.l_BAND(a, b, c); break;
			case OpCode.BOR:   ie.l_BOR(a, b, c); break;
			case OpCode.BXOR:  ie.l_BXOR(a, b, c); break;
			case OpCode.SHL:   ie.l_SHL(a, b, c); break;
			case OpCode.SHR:   ie.l_SHR(a, b, c); break;

			case OpCode.UNM:   ie.l_UNM(a, b); break;
			case OpCode.BNOT:   ie.l_BNOT(a, b); break;
			case OpCode.NOT:   ie.l_NOT(a, b); break;
			case OpCode.LEN:   ie.l_LEN(a, b); break;

			case OpCode.CONCAT:  ie.l_CONCAT(a, b, c); break;

			case OpCode.JMP:  ie.l_JMP(sbx); break;
			case OpCode.EQ:   ie.l_EQ(a, b, c); break;
			case OpCode.LT:   ie.l_LT(a, b, c); break;
			case OpCode.LE:   ie.l_LE(a, b, c); break;

			case OpCode.TEST:     ie.l_TEST(a, c); break;
			case OpCode.TESTSET:  ie.l_TESTSET(a, b, c); break;

			case OpCode.CALL:      ie.l_CALL(a, b, c); break;
			case OpCode.TAILCALL:  ie.l_TAILCALL(a, b, c); break;
			case OpCode.RETURN:    ie.l_RETURN(a, b); break;

			case OpCode.FORLOOP:  ie.l_FORLOOP(a, sbx); break;
			case OpCode.FORPREP:  ie.l_FORPREP(a, sbx); break;
		
			case OpCode.TFORCALL:  ie.l_TFORCALL(a, c); break;
			case OpCode.TFORLOOP:  ie.l_TFORLOOP(a, sbx); break;
		
			case OpCode.SETLIST:  ie.l_SETLIST(a, b, c); break;
		
			case OpCode.CLOSURE:  ie.l_CLOSURE(a, bx); break;
		
			case OpCode.VARARG:  ie.l_VARARG(a, b); break;
		
			case OpCode.EXTRAARG:  ie.l_EXTRAARG(ax); break;
			
			default: throw new UnsupportedOperationException("Unsupported opcode: " + oc);
		}
	}

	@Override
	public void l_MOVE(int a, int b) {
		pushRegister(b);
		pushIntoRegister(a);
	}

	@Override
	public void l_LOADK(int dest, int idx) {
		System.err.println("LOADK " + dest + " " + idx);
		pushConstant(OpCode.indexK(idx));
		pushIntoRegister(dest);
	}

	@Override
	public void l_LOADBOOL(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_LOADNIL(int a, int b) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_GETUPVAL(int a, int b) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_GETTABUP(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_GETTABLE(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_SETTABUP(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_SETUPVAL(int a, int b) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_SETTABLE(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_NEWTABLE(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_SELF(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	private void l_binOp(String method, int dest, int left, int right) {
		System.err.println("BINOP(" + method + ") " + dest + " " + left + " " + right);
		if (OpCode.isK(left)) pushConstant(OpCode.indexK(left)); else pushRegister(left);
		if (OpCode.isK(right)) pushConstant(OpCode.indexK((byte) right)); else pushRegister(right);
		visitMethodInsn(INVOKESTATIC, Type.getInternalName(Operators.class), method, "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
		pushIntoRegister(dest);
	}

	@Override
	public void l_ADD(int a, int b, int c) {
		l_binOp("add", a, b, c);
	}

	@Override
	public void l_SUB(int a, int b, int c) {
		l_binOp("sub", a, b, c);
	}

	@Override
	public void l_MUL(int a, int b, int c) {
		l_binOp("mul", a, b, c);
	}

	@Override
	public void l_MOD(int a, int b, int c) {
		l_binOp("mod", a, b, c);
	}

	@Override
	public void l_POW(int a, int b, int c) {
		l_binOp("pow", a, b, c);
	}

	@Override
	public void l_DIV(int a, int b, int c) {
		l_binOp("div", a, b, c);
	}

	@Override
	public void l_IDIV(int a, int b, int c) {
		l_binOp("idiv", a, b, c);
	}

	@Override
	public void l_BAND(int a, int b, int c) {
		l_binOp("band", a, b, c);
	}

	@Override
	public void l_BOR(int a, int b, int c) {
		l_binOp("bor", a, b, c);
	}

	@Override
	public void l_BXOR(int a, int b, int c) {
		l_binOp("bxor", a, b, c);
	}

	@Override
	public void l_SHL(int a, int b, int c) {
		l_binOp("shl", a, b, c);
	}

	@Override
	public void l_SHR(int a, int b, int c) {
		l_binOp("shr", a, b, c);
	}

	@Override
	public void l_UNM(int a, int b) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_BNOT(int a, int b) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_NOT(int a, int b) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_LEN(int a, int b) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_CONCAT(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_JMP(int sbx) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_EQ(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_LT(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_LE(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}


	@Override
	public void l_TEST(int a, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_TESTSET(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}


	@Override
	public void l_CALL(int a, int b, int c) {

		// TODO: only save relevant registers (from `a' onwards?) to the stack

		if (b != 0) {
			// b - 1 is the exact number of arguments
			saveRegistersToBase(a + 1, a + b - 1);
			setTop(a + b);
		}
		else {
			throw new UnsupportedOperationException("vararg calls not implemented");
		}

		pushRegister(a);  // the function
		visitTypeInsn(CHECKCAST, Type.getInternalName(Function.class));

		// base of the called function
		pushSelf();
		pushInt(a + 1);
		visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Registers.class), "from", Type.getMethodDescriptor(Type.getType(Registers.class), Type.INT_TYPE), true);

		// return address
		pushSelf();
		pushInt(a);
		visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Registers.class), "from", Type.getMethodDescriptor(Type.getType(Registers.class), Type.INT_TYPE), true);

		visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Function.class), CALL_METHOD_NAME, CALL_METHOD_TYPE.getDescriptor(), false);

		// TODO: load registers from a onwards as these have been updated by the called function

		// TODO: handle `c' -- the # of saved results
	}

	@Override
	public void l_TAILCALL(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_RETURN(int a, int b) {

		// TODO: only save relevant registers (from `a' onwards?) to the stack
//		saveRegistersToBase();

//		if (b != 0) {
//			// b - 1 is the exact num of arguments
//			setTop(a + b - 1);
//		}

		// TODO: close upvalues

		if (b != 0) {
			// b - 1 is the exact number of return values
			saveRegistersToRet(a, a + b - 2);
		}
		else {
			// returning up to stack top
			throw new UnsupportedOperationException("vararg return not implemented");
		}

		visitInsn(RETURN);
	}

	@Override
	public void l_FORLOOP(int a, int sbx) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_FORPREP(int a, int sbx) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_TFORCALL(int a, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_TFORLOOP(int a, int sbx) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_SETLIST(int a, int b, int c) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_CLOSURE(int a, int bx) {
		System.err.println("CLOSURE " + a + " " + bx);

		String className = prototypeToClassMap.classNameFor(bx);

		System.err.println("this is prototype #" + bx + ", corresponding to " + className);

		Type functionType = ASMUtils.typeForClassName(className);

		// instantiate the closure
		visitTypeInsn(NEW, functionType.getInternalName());
		visitInsn(DUP);
		// TODO: upvalues
		visitMethodInsn(INVOKESPECIAL, functionType.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);

		pushIntoRegister(a);
	}

	@Override
	public void l_VARARG(int a, int b) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_EXTRAARG(int ax) {
		throw new UnsupportedOperationException("not implemented");
	}

}

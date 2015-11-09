package net.sandius.rembulan.test;

import net.sandius.rembulan.core.OpCode;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.asm.ASMUtils;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

public class LuaBytecodeMethodVisitor extends MethodVisitor implements InstructionEmitter {

	private static Type REGISTERS_TYPE = ASMUtils.arrayTypeFor(Object.class);

	private final Type thisType;
	private final Object[] constants;

	private final int numRegs;

	private Label l_first;
	private Label l_last;
	private Label l_default;
	private Label l_save_and_yield;

	private final int numInstrs;
	private Label[] l_pc;
	private Label[] l_pc_end;
	private Label[] l_pc_preempt;

	protected InstructionEmitter ie;

	public LuaBytecodeMethodVisitor(ClassVisitor cv, Type thisType, Object[] constants, int numInstrs, int numRegs) {
		super(ASM5);
		Check.notNull(cv);
		Check.notNull(thisType);

		this.thisType = thisType;
		this.constants = constants;
		this.numRegs = numRegs;
		this.numInstrs = numInstrs;

		ie = this;

		mv = cv.visitMethod(ACC_PUBLIC, "resume", "()V", null, null);
	}

	public void begin() {
		mv.visitCode();

		l_first = new Label();
		l_last = new Label();
		l_save_and_yield = new Label();
		l_default = new Label();

		// luapc-to-jvmpc mapping
		l_pc = new Label[numInstrs];
		l_pc_end = new Label[numInstrs];
		l_pc_preempt = new Label[numInstrs];
		for (int i = 0; i < l_pc.length; i++) {
			l_pc[i] = new Label();
			l_pc_end[i] = new Label();
			l_pc_preempt[i] = new Label();
		}

		luaCodeBegin();
	}

	public void end() {
		luaCodeEnd();

		mv.visitLabel(l_last);

		mv.visitLocalVariable("this", thisType.getDescriptor(), null, l_first, l_last, 0);

		// registers
		for (int i = 0; i < numRegs; i++) {
			mv.visitLocalVariable("r_" + (i + 1), Type.getDescriptor(Object.class), null, l_first, l_last, i + 1);
		}

		mv.visitMaxs(numRegs + 1, numRegs + 1);

		mv.visitEnd();
	}

	public void luaCodeBegin() {
		preamble();
	}

	public void luaCodeEnd() {

		// save registers and yield
		visitLabel(l_save_and_yield);
		visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		saveRegisters();
		yield();

		// error branch
		visitLabel(l_default);
		visitLineNumber(2, l_default);
		visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		visitTypeInsn(NEW, Type.getInternalName(IllegalStateException.class));
		visitInsn(DUP);
		visitMethodInsn(INVOKESPECIAL, Type.getInternalName(IllegalStateException.class), "<init>", "()V", false);
		visitInsn(ATHROW);
	}

	public void preamble() {

//			for (int i = 0; i < 3; i++) {
//				lmv.visitTryCatchBlock(l_pc[i], l_pc_end[i], l_pc_preempt[i], Type.getInternalName(Yield.class));
//			}

		mv.visitLabel(l_first);
		mv.visitLineNumber(2, l_first);

		loadRegisters();

		Object[] regTypes = new Object[numRegs];
		for (int i = 0; i < regTypes.length; i++) {
			regTypes[i] = Type.getInternalName(Object.class);
		}

		mv.visitFrame(Opcodes.F_APPEND, numRegs, regTypes, 0, null);

		// branch according to the program counter
		preambleSwitch();
	}

	private void preambleSwitch() {
		loadPc();
		mv.visitTableSwitchInsn(0, 2, l_default, l_pc);
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

	private void pushBasePlus(int offset) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, thisType.getInternalName(), "base", "I");
		if (offset > 0) {
			pushInt(offset);
			mv.visitInsn(IADD);
		}
	}

	private void loadRegister(int idx) {
		Check.nonNegative(idx);

		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, thisType.getInternalName(), "objectStack", "Lnet/sandius/rembulan/core/ObjectStack;");
		pushBasePlus(idx);
		mv.visitMethodInsn(INVOKEVIRTUAL, "net/sandius/rembulan/core/ObjectStack", "get", "(I)Ljava/lang/Object;", false);
		mv.visitVarInsn(ASTORE, idx + 1);
	}

	private void saveRegister(int idx) {
		Check.nonNegative(idx);

		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, thisType.getInternalName(), "objectStack", "Lnet/sandius/rembulan/core/ObjectStack;");
		pushBasePlus(idx);
		mv.visitVarInsn(ALOAD, idx + 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "net/sandius/rembulan/core/ObjectStack", "set", "(ILjava/lang/Object;)V", false);
	}

	public void loadRegisters() {
		// load registers into local variables
		for (int i = 0; i < numRegs; i++) {
			loadRegister(i);
		}
	}

	public void saveRegisters() {
		for (int i = 0; i < numRegs; i++) {
			saveRegister(i);
		}
	}

	public void loadPc() {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, thisType.getInternalName(), "pc", Type.INT_TYPE.getDescriptor());
	}

	public void savePc(int pc) {
		mv.visitVarInsn(ALOAD, 0);
		pushInt(pc);
		mv.visitFieldInsn(PUTFIELD, thisType.getInternalName(), "pc", Type.INT_TYPE.getDescriptor());
	}

	public void setTop(int to) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, thisType.getInternalName(), "objectStack", "Lnet/sandius/rembulan/core/ObjectStack;");
		pushBasePlus(to);
		mv.visitMethodInsn(INVOKEVIRTUAL, "net/sandius/rembulan/core/ObjectStack", "setTop", "(I)V", false);
	}

	private void checkPreempt(int pc) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, thisType.getInternalName(), "shouldPreempt", "()Z", false);
		mv.visitJumpInsn(IFEQ, l_pc[pc]);  // continue with pc == 1

		savePc(pc);
		mv.visitJumpInsn(GOTO, l_save_and_yield);
	}


	public void atPc(int pc, int lineNumber) {
		if (pc > 0) {
			mv.visitLabel(l_pc_end[pc - 1]);
			checkPreempt(pc);
		}

		mv.visitLabel(l_pc[pc]);
		if (lineNumber > 0) mv.visitLineNumber(lineNumber, l_pc[pc]);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
	}

	public void yield() {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, thisType.getInternalName(), "preempt", "()V", false);
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

			case OpCode.RETURN: ie.l_RETURN(a, b); break;

			default: throw new UnsupportedOperationException("Unsupported opcode: " + oc);
		}
	}

	@Override
	public void l_MOVE(int a, int b) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void l_LOADK(int dest, int idx) {
		Object c = constants[-idx - 1];

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

		mv.visitVarInsn(ASTORE, dest + 1);
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
		// TODO: swap these?
		mv.visitVarInsn(ALOAD, left + 1);
		mv.visitVarInsn(ALOAD, right + 1);
		mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Operators.class), method, "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
		mv.visitVarInsn(ASTORE, dest + 1);
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
	public void l_RETURN(int a, int b) {


		saveRegisters();

		// FIXME: adjusting stack top
		setTop(1);

		mv.visitInsn(RETURN);  // end; TODO: signal a return!
	}

}

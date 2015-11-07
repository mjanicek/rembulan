package net.sandius.rembulan.test;

import net.sandius.rembulan.core.OpCode;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.asm.ASMUtils;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

public class LuaBytecodeMethodVisitor extends MethodVisitor {

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

	public LuaBytecodeMethodVisitor(ClassVisitor cv, Type thisType, Object[] constants, int numInstrs, int numRegs) {
		super(ASM5);
		Check.notNull(cv);
		Check.notNull(thisType);

		this.thisType = thisType;
		this.constants = constants;
		this.numRegs = numRegs;
		this.numInstrs = numInstrs;

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
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, thisType.getInternalName(), "pc", Type.INT_TYPE.getDescriptor());
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

	public void loadRegisters() {
		// load registers into local variables
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, thisType.getInternalName(), "reg", REGISTERS_TYPE.getDescriptor());
		for (int i = 0; i < numRegs; i++) {
			// reg[i] -> local var i+1
			mv.visitInsn(DUP);
			pushInt(i);
			mv.visitInsn(AALOAD);
			mv.visitVarInsn(ASTORE, i + 1);  // lv[i+1] := reg[i]
		}
		mv.visitInsn(POP);
	}

	public void saveRegisters() {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, thisType.getInternalName(), "reg", REGISTERS_TYPE.getDescriptor());
		for (int i = 0; i < numRegs; i++) {
			mv.visitInsn(DUP);
			pushInt(i);
			mv.visitVarInsn(ALOAD, i + 1);
			mv.visitInsn(AASTORE);  // reg[i] := lv[i+1]
		}
		mv.visitInsn(POP);
	}

	public void savePc(int pc) {
		mv.visitVarInsn(ALOAD, 0);
		pushInt(pc);
		mv.visitFieldInsn(PUTFIELD, thisType.getInternalName(), "pc", Type.INT_TYPE.getDescriptor());
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
			case OpCode.LOADK:  l_LOADK(a, bx); break;
			case OpCode.ADD:    l_ADD(a, b, c); break;
			case OpCode.RETURN: l_RETURN(a, b); break;

			default: throw new UnsupportedOperationException("Unsupported opcode: " + oc);
		}
	}

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

	public void l_ADD(int dest, int left, int right) {
		// TODO: swap these?
		mv.visitVarInsn(ALOAD, left + 1);
		mv.visitVarInsn(ALOAD, right + 1);
		mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Operators.class), "add", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
		mv.visitVarInsn(ASTORE, dest + 1);
	}

	public void l_RETURN(int a, int b) {
		// FIXME: adjusting stack top
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(ICONST_1);
		mv.visitFieldInsn(PUTFIELD, thisType.getInternalName(), "top", Type.INT_TYPE.getDescriptor());

		mv.visitInsn(RETURN);  // end; TODO: signal a return!
	}

}

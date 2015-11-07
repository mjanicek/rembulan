package net.sandius.rembulan.test;

import net.sandius.rembulan.core.CallInfo;
import net.sandius.rembulan.core.Operators;
import net.sandius.rembulan.core.PreemptionContext;
import org.objectweb.asm.*;

public class IncCallInfoDump implements Opcodes {

	private final Type thisType;

	private final int numRegs = 3;

	private final Label[] l_pc;
	private final Label[] l_pc_end;
	private final Label[] l_pc_preempt;
	private final Label l_save_and_yield;

	private final Object[] constants = new Object[] {
			Integer.valueOf(1)
	};

	private static Type arrayTypeFor(Class<?> clazz) {
		return Type.getType("[" + Type.getType(clazz).getDescriptor());
	}

	private static Type REGISTERS_TYPE = arrayTypeFor(Object.class);

	public IncCallInfoDump(String className) {
		this.thisType = Type.getType("L" + className.replace(".", "/") + ";");

		// luapc-to-jvmpc mapping
		this.l_pc = new Label[3];  // got 3 instructions in total
		this.l_pc_end = new Label[3];  // got 3 instructions in total
		this.l_pc_preempt = new Label[3];  // got 3 instructions in total
		for (int i = 0; i < l_pc.length; i++) {
			l_pc[i] = new Label();
			l_pc_end[i] = new Label();
			l_pc_preempt[i] = new Label();
		}

		this.l_save_and_yield = new Label();
	}

	private void emitPushIntConst(MethodVisitor mv, int i) {
		if (i >= -1 && i <= 5) mv.visitInsn(ICONST_0 + i);
		else mv.visitLdcInsn(i);
	}

	private void emitPushLongConst(MethodVisitor mv, long l) {
		if (l >= 0 && l <= 1) mv.visitInsn(LCONST_0 + (int) l);
		else mv.visitLdcInsn(l);
	}

	private void emitPushFloatConst(MethodVisitor mv, float f) {
		if (f == 0.0f) mv.visitInsn(FCONST_0);
		else if (f == 1.0f) mv.visitInsn(FCONST_1);
		else if (f == 2.0f) mv.visitInsn(FCONST_2);
		else mv.visitLdcInsn(f);
	}

	private void emitPushDoubleConst(MethodVisitor mv, double d) {
		if (d == 0.0) mv.visitInsn(DCONST_0);
		else if (d == 1.0) mv.visitInsn(DCONST_1);
		else mv.visitLdcInsn(d);
	}

	private void emitPushStringConst(MethodVisitor mv, String s) {
		mv.visitLdcInsn(s);
	}

	private void emit_LOADK(MethodVisitor mv, int dest, int idx) {
		Object c = constants[-idx - 1];
		if (c instanceof Integer) {
			emitPushIntConst(mv, (Integer) c);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
		}
		else if (c instanceof Long) {
			emitPushLongConst(mv, (Long) c);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
		}
		else if (c instanceof Float) {
			emitPushFloatConst(mv, (Float) c);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
		}
		else if (c instanceof Double) {
			emitPushDoubleConst(mv, (Double) c);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
		}
		else if (c instanceof String) {
			emitPushStringConst(mv, (String) c);
		}
		else {
			throw new IllegalArgumentException("Unsupported constant type: " + c.getClass());
		}

		mv.visitVarInsn(ASTORE, dest + 1);
	}

	private void emit_ADD(MethodVisitor mv, int dest, int left, int right) {
		// TODO: swap these?
		mv.visitVarInsn(ALOAD, left + 1);
		mv.visitVarInsn(ALOAD, right + 1);
		mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Operators.class), "add", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
		mv.visitVarInsn(ASTORE, dest + 1);
	}

	private void emit_RETURN(MethodVisitor mv, int a, int b) {
		// FIXME: adjusting stack top
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(ICONST_1);
		mv.visitFieldInsn(PUTFIELD, thisType.getInternalName(), "top", Type.INT_TYPE.getDescriptor());

		mv.visitInsn(RETURN);  // end; TODO: signal a return!
	}

	private void emitPreambleSwitch(MethodVisitor mv, Label l_default) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, thisType.getInternalName(), "pc", Type.INT_TYPE.getDescriptor());
		mv.visitTableSwitchInsn(0, 2, l_default, l_pc);
	}

	private void emitAtPc(MethodVisitor mv, int pc, int lineNumber) {
		if (pc > 0) {
			mv.visitLabel(l_pc_end[pc - 1]);
			emitCheckPreempt(mv, pc, l_pc, l_save_and_yield);
		}

		mv.visitLabel(l_pc[pc]);
		if (lineNumber > 0) mv.visitLineNumber(lineNumber, l_pc[pc]);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
	}

	private void emitSavePcAndJump(MethodVisitor mv, int pc, Label label) {
		mv.visitLabel(l_pc_preempt[pc]);
		// TODO: must contain frame!!
		mv.visitVarInsn(ALOAD, 0);
		emitPushIntConst(mv, pc);
		mv.visitFieldInsn(PUTFIELD, thisType.getInternalName(), "pc", Type.INT_TYPE.getDescriptor());
		mv.visitJumpInsn(GOTO, label);  // jump
	}

	private void emitCheckPreempt(MethodVisitor mv, int pc, Label[] l_pc, Label l_save_and_yield) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, thisType.getInternalName(), "shouldPreempt", "()Z", false);
		mv.visitJumpInsn(IFEQ, l_pc[pc]);  // continue with pc == 1
		emitSavePcAndJump(mv, pc, l_save_and_yield);
	}

	private void emitLoadRegisters(MethodVisitor mv) {
		// load registers into local variables
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, thisType.getInternalName(), "reg", REGISTERS_TYPE.getDescriptor());
		for (int i = 0; i < numRegs; i++) {
			// reg[i] -> local var i+1
			mv.visitInsn(DUP);
			emitPushIntConst(mv, i);
			mv.visitInsn(AALOAD);
			mv.visitVarInsn(ASTORE, i + 1);  // lv[i+1] := reg[i]
		}
		mv.visitInsn(POP);
	}

	private void emitSaveRegisters(MethodVisitor mv) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, thisType.getInternalName(), "reg", REGISTERS_TYPE.getDescriptor());
		for (int i = 0; i < numRegs; i++) {
			mv.visitInsn(DUP);
			emitPushIntConst(mv, i);
			mv.visitVarInsn(ALOAD, i + 1);
			mv.visitInsn(AASTORE);  // reg[i] := lv[i+1]
		}
		mv.visitInsn(POP);
	}

	private void emitYield(MethodVisitor mv) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, thisType.getInternalName(), "preempt", "()V", false);
	}

	public byte[] dump() throws Exception {

		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, thisType.getInternalName(), null, Type.getInternalName(CallInfo.class), null);

		cw.visitSource("inc.lua", null);

		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lnet/sandius/rembulan/core/PreemptionContext;I)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(10, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(CallInfo.class), "<init>", "(Lnet/sandius/rembulan/core/PreemptionContext;I)V", false);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(11, l1);
			mv.visitInsn(RETURN);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLocalVariable("this", thisType.getDescriptor(), null, l0, l2, 0);
			mv.visitLocalVariable("context", Type.getDescriptor(PreemptionContext.class), null, l0, l2, 1);
			mv.visitLocalVariable("max", Type.INT_TYPE.getDescriptor(), null, l0, l2, 2);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "resume", "()V", null, null);
			mv.visitCode();

//			for (int i = 0; i < 3; i++) {
//				mv.visitTryCatchBlock(l_pc[i], l_pc_end[i], l_pc_preempt[i], Type.getInternalName(Yield.class));
//			}

			// first label
			Label l_first = new Label();
			mv.visitLabel(l_first);
			mv.visitLineNumber(2, l_first);

			emitLoadRegisters(mv);

			Object[] regTypes = new Object[numRegs];
			for (int i = 0; i < regTypes.length; i++) {
				regTypes[i] = Type.getInternalName(Object.class);
			}

			mv.visitFrame(Opcodes.F_APPEND, numRegs, regTypes, 0, null);

			// branch according to the program counter
			Label l_default = new Label();  // default (error) branch
			emitPreambleSwitch(mv, l_default);

			// resuming at pc == 0

			emitAtPc(mv, 0, 2);
			emit_LOADK(mv, 1, -1);

			emitAtPc(mv, 1, 3);
			emit_ADD(mv, 2, 0, 1);

			emitAtPc(mv, 2, 3);
			emit_RETURN(mv, 2, 2);

			// save registers and yield
			mv.visitLabel(l_save_and_yield);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			emitSaveRegisters(mv);
			emitYield(mv);

			// error branch
			mv.visitLabel(l_default);
			mv.visitLineNumber(2, l_default);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitTypeInsn(NEW, Type.getInternalName(IllegalStateException.class));
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(IllegalStateException.class), "<init>", "()V", false);
			mv.visitInsn(ATHROW);

			// last label
			Label l_last = new Label();
			mv.visitLabel(l_last);

			mv.visitLocalVariable("this", thisType.getDescriptor(), null, l_first, l_last, 0);

			// registers
			for (int i = 0; i < numRegs; i++) {
				mv.visitLocalVariable("r_" + (i + 1), Type.getDescriptor(Object.class), null, l_first, l_last, i + 1);
			}

			mv.visitMaxs(4, numRegs + 1);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}

}

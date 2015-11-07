package net.sandius.rembulan.test;

import net.sandius.rembulan.core.CallInfo;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.util.asm.ASMUtils;
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

	public IncCallInfoDump(String className) {
		this.thisType = ASMUtils.typeForClassName(className);

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

	private void emitPreambleSwitch(MethodVisitor mv, Label l_default) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, thisType.getInternalName(), "pc", Type.INT_TYPE.getDescriptor());
		mv.visitTableSwitchInsn(0, 2, l_default, l_pc);
	}

	private void emitAtPc(LuaBytecodeMethodVisitor mv, int pc, int lineNumber) {
		if (pc > 0) {
			mv.visitLabel(l_pc_end[pc - 1]);
			emitCheckPreempt(mv, pc, l_pc, l_save_and_yield);
		}

		mv.visitLabel(l_pc[pc]);
		if (lineNumber > 0) mv.visitLineNumber(lineNumber, l_pc[pc]);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
	}

	private void emitCheckPreempt(LuaBytecodeMethodVisitor mv, int pc, Label[] l_pc, Label l_save_and_yield) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, thisType.getInternalName(), "shouldPreempt", "()Z", false);
		mv.visitJumpInsn(IFEQ, l_pc[pc]);  // continue with pc == 1

		mv.savePc(pc);
		mv.visitJumpInsn(GOTO, l_save_and_yield);
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
			LuaBytecodeMethodVisitor lmv = new LuaBytecodeMethodVisitor(cw, thisType, constants, numRegs);
			lmv.begin();

//			for (int i = 0; i < 3; i++) {
//				lmv.visitTryCatchBlock(l_pc[i], l_pc_end[i], l_pc_preempt[i], Type.getInternalName(Yield.class));
//			}

			// first label
			Label l_first = new Label();
			lmv.visitLabel(l_first);
			lmv.visitLineNumber(2, l_first);

			lmv.loadRegisters();

			Object[] regTypes = new Object[numRegs];
			for (int i = 0; i < regTypes.length; i++) {
				regTypes[i] = Type.getInternalName(Object.class);
			}

			lmv.visitFrame(Opcodes.F_APPEND, numRegs, regTypes, 0, null);

			// branch according to the program counter
			Label l_default = new Label();  // default (error) branch
			emitPreambleSwitch(lmv, l_default);

			// resuming at pc == 0

			emitAtPc(lmv, 0, 2);
			lmv.l_LOADK(1, -1);

			emitAtPc(lmv, 1, 3);
			lmv.l_ADD(2, 0, 1);

			emitAtPc(lmv, 2, 3);
			lmv.l_RETURN(2, 2);

			// save registers and yield
			lmv.visitLabel(l_save_and_yield);
			lmv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			lmv.saveRegisters();
			lmv.yield();

			// error branch
			lmv.visitLabel(l_default);
			lmv.visitLineNumber(2, l_default);
			lmv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			lmv.visitTypeInsn(NEW, Type.getInternalName(IllegalStateException.class));
			lmv.visitInsn(DUP);
			lmv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(IllegalStateException.class), "<init>", "()V", false);
			lmv.visitInsn(ATHROW);

			// last label
			Label l_last = new Label();
			lmv.visitLabel(l_last);

			lmv.visitLocalVariable("this", thisType.getDescriptor(), null, l_first, l_last, 0);

			// registers
			for (int i = 0; i < numRegs; i++) {
				lmv.visitLocalVariable("r_" + (i + 1), Type.getDescriptor(Object.class), null, l_first, l_last, i + 1);
			}

			lmv.visitMaxs(4, numRegs + 1);

			lmv.end();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}

}

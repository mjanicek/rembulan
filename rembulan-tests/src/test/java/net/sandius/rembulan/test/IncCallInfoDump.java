package net.sandius.rembulan.test;

import net.sandius.rembulan.core.CallInfo;
import net.sandius.rembulan.core.PreemptionContext;
import net.sandius.rembulan.util.asm.ASMUtils;
import org.objectweb.asm.*;

public class IncCallInfoDump implements Opcodes {

	private final Type thisType;

	private final Object[] constants = new Object[] {
			Integer.valueOf(1)
	};

	public IncCallInfoDump(String className) {
		this.thisType = ASMUtils.typeForClassName(className);
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
			LuaBytecodeMethodVisitor lmv = new LuaBytecodeMethodVisitor(cw, thisType, constants, 3, 3);
			lmv.begin();

			lmv.atPc(0, 2);
			lmv.l_LOADK(1, -1);

			lmv.atPc(1, 3);
			lmv.l_ADD(2, 0, 1);

			lmv.atPc(2, 3);
			lmv.l_RETURN(2, 2);

			lmv.end();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}

}

package net.sandius.rembulan.test;

import net.sandius.rembulan.core.CallInfo;
import net.sandius.rembulan.gen.LuaBytecodeMethodVisitor;
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

		LuaBytecodeMethodVisitor.emitConstructor(cw, thisType);

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

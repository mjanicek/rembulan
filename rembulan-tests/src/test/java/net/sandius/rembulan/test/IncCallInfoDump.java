package net.sandius.rembulan.test;

import net.sandius.rembulan.core.Function;
import net.sandius.rembulan.gen.LuaBytecodeMethodVisitor;
import net.sandius.rembulan.util.asm.ASMUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

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
		ClassVisitor cv = cw; // new TraceClassVisitor(cw, new PrintWriter(System.out));
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cv.visit(V1_7, ACC_PUBLIC + ACC_SUPER, thisType.getInternalName(), null, Type.getInternalName(Function.class), null);

		cv.visitSource("inc.lua", null);

		LuaBytecodeMethodVisitor.emitConstructor(cv, thisType);

		{
			LuaBytecodeMethodVisitor lmv = new LuaBytecodeMethodVisitor(cv, thisType, constants, 3, 3);
			lmv.begin();

			lmv.atPc(0, 2);
			lmv.l_LOADK(1, -1);

			lmv.atPc(1, 3);
			lmv.l_ADD(2, 0, 1);

			lmv.atPc(2, 3);
			lmv.l_RETURN(2, 2);

			lmv.end();
		}
		cv.visitEnd();

		return cw.toByteArray();
	}

}

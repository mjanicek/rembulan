package net.sandius.rembulan.test;

import net.sandius.rembulan.core.ArrayBackedConstants;
import net.sandius.rembulan.core.Constants;
import net.sandius.rembulan.core.Invokable;
import net.sandius.rembulan.core.OpCode;
import net.sandius.rembulan.core.gen.LuaBytecodeMethodVisitor;
import net.sandius.rembulan.util.ReadOnlyArray;
import net.sandius.rembulan.util.asm.ASMUtils;
import org.objectweb.asm.*;

public class IncCallInfoDump implements Opcodes {

	private final Type thisType;

	private final Constants constants = new ArrayBackedConstants(ReadOnlyArray.copyFrom(new Object[] {
			3L,
			39L
	}));

	public IncCallInfoDump(String className) {
		this.thisType = ASMUtils.typeForClassName(className);
	}

	public byte[] dump() throws Exception {

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		ClassVisitor cv = cw; // new TraceClassVisitor(cw, new PrintWriter(System.out));
//		ClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.err));

		cv.visit(V1_7, ACC_PUBLIC + ACC_SUPER, thisType.getInternalName(), null, Type.getInternalName(Invokable.class), null);

		cv.visitSource("=stdin", null);

		LuaBytecodeMethodVisitor.emitConstructor(cv, thisType);

		{
			LuaBytecodeMethodVisitor lmv = new LuaBytecodeMethodVisitor(cv, thisType, constants, null, null, 4, 2);
			lmv.begin();

			lmv.atPc(0, 1);
			lmv.l_LOADK(0, 0);

			lmv.atPc(1, 2);
			lmv.l_ADD(1, 0, OpCode.BITRK | 1);

			lmv.atPc(2, 2);
			lmv.l_RETURN(1, 2);

			lmv.atPc(3, 2);
			lmv.l_RETURN(0, 1);

			lmv.end();
		}
		cv.visitEnd();

		return cw.toByteArray();
	}

}

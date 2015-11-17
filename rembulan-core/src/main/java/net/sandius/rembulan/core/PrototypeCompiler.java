package net.sandius.rembulan.core;

import net.sandius.rembulan.gen.LuaBytecodeMethodVisitor;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.asm.ASMUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

import static org.objectweb.asm.Opcodes.*;

public class PrototypeCompiler {

	public static byte[] compile(Prototype proto, String className) {
		Check.notNull(proto);
		Check.notNull(className);

		Type thisType = ASMUtils.typeForClassName(className);

		ClassWriter cw = new ClassWriter(0);
//		ClassVisitor cv = cw; // new TraceClassVisitor(cw, new PrintWriter(System.out));
		ClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cv.visit(V1_7, ACC_PUBLIC + ACC_SUPER, thisType.getInternalName(), null, Type.getInternalName(Function.class), null);
		cv.visitSource(proto.getSource(), null);

		// constructor
		LuaBytecodeMethodVisitor.emitConstructor(cv, thisType);

		// function body
		IntVector code = proto.getCode();

		LuaBytecodeMethodVisitor lmv = new LuaBytecodeMethodVisitor(cv, thisType, proto.getConstants(), proto.getCode().length(), proto.getMaximumStackSize());
		lmv.begin();

		for (int i = 0; i < code.length(); i++) {
			lmv.atPc(i, proto.getLineAtPC(i));
			lmv.instruction(code.get(i));
		}

		lmv.end();

		cv.visitEnd();

		return cw.toByteArray();
	}

}

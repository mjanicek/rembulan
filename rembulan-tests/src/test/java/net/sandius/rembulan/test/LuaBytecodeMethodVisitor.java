package net.sandius.rembulan.test;

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

	public LuaBytecodeMethodVisitor(ClassVisitor cv, Type thisType, Object[] constants, int numRegs) {
		super(ASM5);
		Check.notNull(cv);
		Check.notNull(thisType);

		this.thisType = thisType;
		this.constants = constants;
		this.numRegs = numRegs;

		mv = cv.visitMethod(ACC_PUBLIC, "resume", "()V", null, null);
	}

	public void begin() {
		mv.visitCode();
	}

	public void end() {
		mv.visitEnd();
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

	public void yield() {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, thisType.getInternalName(), "preempt", "()V", false);
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

package net.sandius.rembulan.compiler.gen.asm;

import net.sandius.rembulan.compiler.gen.block.LuaBinaryOperation;
import net.sandius.rembulan.compiler.gen.block.LuaInstruction;
import net.sandius.rembulan.core.Dispatch;
import net.sandius.rembulan.core.ExecutionContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.Arrays;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class DispatchMethods {

	private DispatchMethods() {
		// not to be instantiated
	}

	public static final String OP_ADD = "add";
	public static final String OP_SUB = "sub";
	public static final String OP_MUL = "mul";
	public static final String OP_DIV = "div";
	public static final String OP_MOD = "mod";
	public static final String OP_POW = "pow";
	public static final String OP_UNM = "unm";
	public static final String OP_IDIV = "idiv";

	public static final String OP_BAND = "band";
	public static final String OP_BOR = "bor";
	public static final String OP_BXOR = "bxor";
	public static final String OP_BNOT = "bnot";
	public static final String OP_SHL = "shl";
	public static final String OP_SHR = "shr";

	public static final String OP_CONCAT = "concat";
	public static final String OP_LEN = "len";

	public static final String OP_EQ = "eq";
	public static final String OP_NEQ = "neq";
	public static final String OP_LT = "lt";
	public static final String OP_LE = "le";

	public static final String OP_INDEX = "index";
	public static final String OP_NEWINDEX = "newindex";

	public static final String OP_CALL = "call";

	public static String binaryOperationMethodName(LuaBinaryOperation.Op op) {
		switch (op) {
			case ADD:  return OP_ADD;
			case SUB:  return OP_SUB;
			case MUL:  return OP_MUL;
			case MOD:  return OP_MOD;
			case POW:  return OP_POW;
			case DIV:  return OP_DIV;
			case IDIV: return OP_IDIV;
			case BAND: return OP_BAND;
			case BOR:  return OP_BOR;
			case BXOR: return OP_BXOR;
			case SHL:  return OP_SHL;
			case SHR:  return OP_SHR;
			default: throw new IllegalArgumentException("Illegal binary operation: " + op);
		}
	}

	public static String comparisonMethodName(LuaInstruction.Comparison op) {
		switch (op) {
			case EQ: return OP_EQ;
			case LT: return OP_LT;
			case LE: return OP_LE;
			default: throw new IllegalArgumentException("Illegal comparison operation: " + op);
		}
	}
	
	public static AbstractInsnNode dynamic(String methodName, int numArgs) {
		ArrayList<Type> args = new ArrayList<>();
		args.add(Type.getType(ExecutionContext.class));
		for (int i = 0; i < numArgs; i++) {
			args.add(Type.getType(Object.class));
		}
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				methodName,
				Type.getMethodDescriptor(
						Type.VOID_TYPE,
						args.toArray(new Type[0])),
				false);
	}

	public static AbstractInsnNode numeric(String methodName, int numArgs) {
		Type[] args = new Type[numArgs];
		Arrays.fill(args, Type.getType(Number.class));
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				methodName,
				Type.getMethodDescriptor(
						Type.getType(Number.class),
						args),
				false);
	}

	public static AbstractInsnNode index() {
		return dynamic(OP_INDEX, 2);
	}

	public static AbstractInsnNode newindex() {
		return dynamic(OP_NEWINDEX, 3);
	}

	public static int adjustKind_call(int kind) {
		return kind > 0 ? (call_method(kind).exists() ? kind : 0) : 0;
	}

	public final static int MAX_CALL_KIND;
	static {
		int k = 1;
		while (call_method(k).exists()) k += 1;
		MAX_CALL_KIND = k - 1;
	}

	private static ReflectionUtils.Method call_method(int kind) {
		return ReflectionUtils.staticArgListMethodFromKind(
				Dispatch.class, OP_CALL, new Class[] { ExecutionContext.class, Object.class }, kind);
	}

	public static AbstractInsnNode call(int kind) {
		return call_method(kind).toMethodInsnNode();
	}

	public static AbstractInsnNode continueLoop() {
		return new MethodInsnNode(
				INVOKESTATIC,
				Type.getInternalName(Dispatch.class),
				"continueLoop",
				Type.getMethodDescriptor(
						Type.BOOLEAN_TYPE,
						Type.getType(Number.class),
						Type.getType(Number.class),
						Type.getType(Number.class)),
				false);
	}

}

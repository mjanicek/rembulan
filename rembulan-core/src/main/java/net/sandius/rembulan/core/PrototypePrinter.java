package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.io.PrintStream;

public class PrototypePrinter {

	private static void padSpaces(PrintStream out, int num) {
		for (int i = 0; i < num; i++) {
			out.print(' ');
		}
	}

	public static String opcodeName(int opcode) {
		switch (opcode) {
			case OpCode.MOVE: return "MOVE";
			case OpCode.LOADK: return "LOADK";
			case OpCode.LOADKX: return "LOADKX";
			case OpCode.LOADBOOL: return "LOADBOOL";
			case OpCode.LOADNIL: return "LOADNIL";
			case OpCode.GETUPVAL: return "GETUPVAL";
			case OpCode.GETTABUP: return "GETTABUP";
			case OpCode.GETTABLE: return "GETTABLE";
			case OpCode.SETTABUP: return "SETTABUP";
			case OpCode.SETUPVAL: return "SETUPVAL";
			case OpCode.SETTABLE: return "SETTABLE";
			case OpCode.NEWTABLE: return "NEWTABLE";
			case OpCode.SELF: return "SELF";
			case OpCode.ADD: return "ADD";
			case OpCode.SUB: return "SUB";
			case OpCode.MUL: return "MUL";
			case OpCode.MOD: return "MOD";
			case OpCode.POW: return "POW";
			case OpCode.DIV: return "DIV";
			case OpCode.IDIV: return "IDIV";
			case OpCode.BAND: return "BAND";
			case OpCode.BOR: return "BOR";
			case OpCode.BXOR: return "BXOR";
			case OpCode.SHL: return "SHL";
			case OpCode.SHR: return "SHR";
			case OpCode.UNM: return "UNM";
			case OpCode.BNOT: return "BNOT";
			case OpCode.NOT: return "NOT";
			case OpCode.LEN: return "LEN";
			case OpCode.CONCAT: return "CONCAT";
			case OpCode.JMP: return "JMP";
			case OpCode.EQ: return "EQ";
			case OpCode.LT: return "LT";
			case OpCode.LE: return "LE";
			case OpCode.TEST: return "TEST";
			case OpCode.TESTSET: return "TESTSET";
			case OpCode.CALL: return "CALL";
			case OpCode.TAILCALL: return "TAILCALL";
			case OpCode.RETURN: return "RETURN";
			case OpCode.FORLOOP: return "FORLOOP";
			case OpCode.FORPREP: return "FORPREP";
			case OpCode.TFORCALL: return "TFORCALL";
			case OpCode.TFORLOOP: return "TFORLOOP";
			case OpCode.SETLIST: return "SETLIST";
			case OpCode.CLOSURE: return "CLOSURE";
			case OpCode.VARARG: return "VARARG";
			case OpCode.EXTRAARG: return "EXTRAARG";
			default: throw new IllegalArgumentException("Unknown opcode: " + opcode);
		}
	}

	private static String plural(int n, String sg, String pl) {
		return n + " " + (n == 0 || n > 1 ? pl : sg);
	}

	private static String plural(int n, boolean orMore, String sg, String pl) {
		return n + (orMore ? "+" : "") + " " + (orMore || n == 0 || n > 1 ? pl : sg);
	}

	public static void print(Prototype proto, PrintStream out) {
		print(proto, out, true);
	}

	private static String pseudoAddr(Prototype proto) {
		Check.notNull(proto);
		return "0x" + Integer.toHexString(System.identityHashCode(proto));
	}

	private static String escape(String s) {
		return "\"" + s + "\"";  // FIXME!
	}

	private static String prettyPrint(Object o) {
		Check.notNull(o);

		if (o instanceof Prototype) return pseudoAddr((Prototype) o);
		if (o instanceof String) return escape((String) o);
		if (o instanceof Number) return Conversions.numberToLuaFormatString((Number) o);

		return o.toString();
	}

	public static void print(Prototype proto, PrintStream out, boolean isMain) {
		Check.notNull(proto);
		Check.notNull(out);

		IntVector code = proto.getCode();
		ReadOnlyArray<Object> constants = proto.getConstants();
		ReadOnlyArray<LocalVariable> locals = proto.getLocalVariables();
		ReadOnlyArray<Upvalue.Desc> upvalues = proto.getUpValueDescriptions();
		ReadOnlyArray<Prototype> nested = proto.getNestedPrototypes();

		// 1st line
		out.println(
				(isMain ? "main" : "function")
				+ " <" + proto.getShortSource() + ":" + proto.getBeginLine() + "," + proto.getEndLine() + ">"
				+ " (" + code.length() + " instructions at " + pseudoAddr(proto) + ")"
		);

		// 2nd line
		out.println(
				plural(proto.getNumberOfParameters(), proto.isVararg(), "param", "params")
				+ ", " + plural(proto.getMaximumStackSize(), "slot", "slots")
				+ ", " + plural(upvalues.size(), "upvalue", "upvalues")
				+ ", " + plural(locals.size(), "local", "locals")
				+ ", " + plural(constants.size(), "constant", "constants")
				+ ", " +  plural(nested.size(), "function", "functions")
		);

		// code listing
		for (int i = 0; i < code.length(); i++) {
			out.print('\t');
			out.print(i + 1);  // program counter
			out.print('\t');
			out.print("[" + proto.getLineAtPC(i) + "]");  // line number
			out.print('\t');

			int insn = code.get(i);
			int opcode = OpCode.opCode(insn);
			int a = OpCode.arg_A(insn);
			int b = OpCode.arg_B(insn);
			int c = OpCode.arg_C(insn);
			int bx = OpCode.arg_Bx(insn);
			int sbx = OpCode.arg_sBx(insn);
			int ax = OpCode.arg_Ax(insn);

			String name = opcodeName(opcode);
			out.print(name);  // opcode

			padSpaces(out, 9 - name.length());
			out.print('\t');

			String hint = "";

			// instruction arguments
			switch (OpCode.getOpMode(opcode)) {
				case OpCode.iABC:
					out.print(a);
					switch (OpCode.getBMode(opcode)) {
						case OpCode.OpArgN: break;
						case OpCode.OpArgU: out.print(" " + b); break;
						case OpCode.OpArgR: out.print(" " + b); break;
 						case OpCode.OpArgK:
							out.print(" ");
							if (OpCode.isK(b)) {
								out.print(-1 - OpCode.indexK(b));
								hint = prettyPrint(constants.get(OpCode.indexK(b)));
							}
							else {
								out.print(b);
								hint = "-";
							}
							break;
					}

					switch (OpCode.getCMode(opcode)) {
						case OpCode.OpArgN: break;
						case OpCode.OpArgU: out.print(" " + c); break;
						case OpCode.OpArgR: out.print(" " + c); break;
 						case OpCode.OpArgK:
							out.print(" ");
							if (OpCode.isK(c)) {
								out.print(-1 - OpCode.indexK(c));
								hint = (hint.isEmpty() ? "-" : hint) + " " + prettyPrint(constants.get(OpCode.indexK(c)));
							}
							else {
								out.print(c);
								hint = (hint.isEmpty() ? hint : hint + " -");
							}
							break;
					}
					break;

				case OpCode.iABx:
					out.print(a);
					switch (OpCode.getBMode(opcode)) {
						case OpCode.OpArgN: break;
						case OpCode.OpArgU: out.print(" " + bx); break;
						case OpCode.OpArgR: out.print(" " + bx); break;
 						case OpCode.OpArgK:
							out.print(" " + (-1 - OpCode.indexK(bx)));
							hint += prettyPrint(constants.get(OpCode.indexK(bx)));
							break;
					}
					break;

				case OpCode.iAx:
					out.print(ax);
					break;

				case OpCode.iAsBx:
					out.print(a + " " + sbx);
					break;
				default:
			}

			// additional hints
			switch (opcode) {
				case OpCode.CLOSURE:
					hint = prettyPrint(nested.get(ax));
					break;
			}

			if (!hint.isEmpty()) {
				out.print("\t; " + hint);
			}

			out.println();
		}

		// constants
		out.println("constants (" + constants.size() + ") for " + pseudoAddr(proto) + ":");
		for (int i = 0; i < constants.size(); i++) {
			out.print('\t');
			out.print(i + 1);  // index
			out.print('\t');

			Object c = constants.get(i);

			out.print(prettyPrint(c));

			out.println();
		}

		// locals
		out.println("locals (" + locals.size() + ") for " + pseudoAddr(proto) + ":");
		for (int i = 0; i < locals.size(); i++) {
			out.print('\t');
			out.print(i);  // index
			out.print('\t');

			LocalVariable lv = locals.get(i);
			out.print(lv.variableName + "\t" + (lv.beginPC + 1) + "\t" + (lv.endPC + 1));
			out.println();
		}

		// upvalues
		out.println("upvalues (" + upvalues.size() + ") for " + pseudoAddr(proto) + ":");
		for (int i = 0; i < upvalues.size(); i++) {
			out.print('\t');
			out.print(i);  // index
			out.print('\t');

			Upvalue.Desc uvd = upvalues.get(i);
			out.print(uvd.name + "\t" + (uvd.inStack ? 1 : 0) + "\t" + uvd.index);
			out.println();
		}

		// nested prototypes
		for (Prototype np : nested) {
			out.println();
			print(np, out, false);
		}

	}

}

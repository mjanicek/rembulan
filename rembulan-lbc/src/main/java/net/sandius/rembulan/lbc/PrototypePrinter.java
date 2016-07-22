package net.sandius.rembulan.lbc;

import net.sandius.rembulan.LuaFormat;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

public class PrototypePrinter {

	private static String spaces(int num) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < num; i++) {
			out.append(' ');
		}
		return out.toString();
	}

	private static String plural(int n, String sg, String pl) {
		return n + " " + (n == 0 || n > 1 ? pl : sg);
	}

	private static String plural(int n, boolean orMore, String sg, String pl) {
		return n + (orMore ? "+" : "") + " " + (orMore || n == 0 || n > 1 ? pl : sg);
	}

	public static void print(Prototype proto, PrintWriter out) {
		print(proto, out, true);
	}

	public static String toString(Prototype proto) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		print(proto, new PrintWriter(baos));
		return new String(baos.toByteArray());
	}

	public static String pseudoAddr(Prototype proto) {
		Check.notNull(proto);
		return "0x" + Integer.toHexString(System.identityHashCode(proto));
	}

	public static String escape(String s) {
		StringBuilder bld = new StringBuilder();

		byte[] bytes = s.getBytes();
		for (byte b : bytes) {
			char c = (char) b;
			if (c >= ' ' && c <= '~' && c != '\"' && c != '\\') {
				bld.append((char) b);
			}
			else {
				bld.append('\\');
				switch (c) {
					case '"':  bld.append('"'); break;
					case '\\': bld.append("\\"); break;
					case (char) 0x07: bld.append('a'); break;  // bell
					case '\b': bld.append('b'); break;  // backspace
					case '\f': bld.append('f'); break;  // form feed
					case '\t': bld.append('t'); break;  // tab
					case '\r': bld.append("r"); break;  // carriage return
					case '\n': bld.append('n'); break;  // newline
					case (char) 0x0b: bld.append('v'); break;  // vertical tab
					default:
						bld.append(Integer.toString(1000 + (0xff & c)).substring(1));
				}
			}
		}
		
		return bld.toString();
	}

	private static String constantToString(Object c) {
		if (c == null) return LuaFormat.NIL;
		else if (c instanceof Boolean) return LuaFormat.toString((Boolean) c);
		else if (c instanceof Long) return LuaFormat.toString((Long) c);
		else if (c instanceof Double) return LuaFormat.toString((Double) c);
		else if (c instanceof String) return "\"" + escape((String) c) + "\"";
		else return null;
	}

	private static String constantToString(ReadOnlyArray<Object> constants, int idx) {
		Check.notNull(constants);
		Object c = constants.get(idx);
		String s = constantToString(c);

		if (s != null) {
			return s;
		}
		else {
			throw new IllegalArgumentException("Unknown constant #" + idx + "(" + c + ")");
		}
	}

	public static String instructionInfo(int insn) {
		StringBuilder out = new StringBuilder();

		int opcode = OpCode.opCode(insn);
		int a = OpCode.arg_A(insn);
		int b = OpCode.arg_B(insn);
		int c = OpCode.arg_C(insn);
		int bx = OpCode.arg_Bx(insn);
		int sbx = OpCode.arg_sBx(insn);
		int ax = OpCode.arg_Ax(insn);

		String name = OpCode.opcodeName(opcode);
		out.append(name);  // opcode

		out.append(spaces(9 - name.length()));
		out.append('\t');

		StringBuilder hint = new StringBuilder();

		// instruction arguments
		switch (OpCode.getOpMode(opcode)) {
			case OpCode.iABC:
				out.append(a);
				switch (OpCode.getBMode(opcode)) {
					case OpCode.OpArgN: break;
					case OpCode.OpArgU: out.append(" ").append(b); break;
					case OpCode.OpArgR: out.append(" ").append(b); break;
						case OpCode.OpArgK:
						out.append(" ");
						if (OpCode.isK(b)) {
							out.append(-1 - OpCode.indexK(b));
						}
						else {
							out.append(b);
						}
						break;
				}

				switch (OpCode.getCMode(opcode)) {
					case OpCode.OpArgN: break;
					case OpCode.OpArgU: out.append(" ").append(c); break;
					case OpCode.OpArgR: out.append(" ").append(c); break;
						case OpCode.OpArgK:
						out.append(" ");
						out.append(OpCode.isK(c) ? -1 - OpCode.indexK(c) : c);
						break;
				}
				break;

			case OpCode.iABx:
				out.append(a);
				switch (OpCode.getBMode(opcode)) {
					case OpCode.OpArgN: break;
					case OpCode.OpArgU: out.append(" ").append(bx); break;
					case OpCode.OpArgR: out.append(" ").append(bx); break;
					case OpCode.OpArgK:
						out.append(" ").append(-1 - OpCode.indexK(bx));
						break;
				}
				break;

			case OpCode.iAx:
				out.append(ax);
				break;

			case OpCode.iAsBx:
				out.append(a).append(" ").append(sbx);
				break;
			default:
		}

		return out.toString();
	}

	public static String instructionInfoWithHints(Prototype proto, int pc) {
		Check.notNull(proto);
		Check.nonNegative(pc);

		return instructionInfoWithHints(proto.getCode().get(pc), pc + 1, proto.getConstants(), proto.getNestedPrototypes());
	}

	public static String instructionInfoHints(int insn, int pc, ReadOnlyArray<Object> constants, ReadOnlyArray<Prototype> children) {
		int opcode = OpCode.opCode(insn);
		int a = OpCode.arg_A(insn);
		int b = OpCode.arg_B(insn);
		int c = OpCode.arg_C(insn);
		int bx = OpCode.arg_Bx(insn);
		int sbx = OpCode.arg_sBx(insn);
		int ax = OpCode.arg_Ax(insn);

		StringBuilder hint = new StringBuilder();

		// instruction arguments
		switch (OpCode.getOpMode(opcode)) {
			case OpCode.iABC:
				if (OpCode.getBMode(opcode) == OpCode.OpArgK) {
					hint.append(OpCode.isK(b) ? constantToString(constants, OpCode.indexK(b)) : "-");
				}

				if (OpCode.getCMode(opcode) == OpCode.OpArgK) {
					if (OpCode.isK(c)) {
						(hint.length() > 0 ? hint : hint.append("-")).append(" ").append(constantToString(constants, OpCode.indexK(c)));
					}
					else {
						(hint.length() > 0 ? hint : hint.append("-")).append(" ").append("-");
					}
				}
				break;

			case OpCode.iABx:
				if (OpCode.getBMode(opcode) == OpCode.OpArgK) {
					hint.append(constantToString(constants, OpCode.indexK(bx)));
				}
				break;

			default:
				break;
		}

		// additional hints
		switch (opcode) {
			case OpCode.CLOSURE:
				hint.append(pseudoAddr(children.get(bx)));
				break;

			case OpCode.JMP:
			case OpCode.FORLOOP:
			case OpCode.FORPREP:
			{
				int destPC = pc + sbx + 1;
				hint.append("to ").append(destPC);
			}
				break;
		}

		return hint.toString();
	}

	public static String instructionInfoWithHints(int insn, int pc, ReadOnlyArray<Object> constants, ReadOnlyArray<Prototype> children) {
		Check.notNull(constants);
		Check.notNull(children);

		String instrInfo = instructionInfo(insn);
		String hint = instructionInfoHints(insn, pc, constants, children);
		return instrInfo + (!hint.isEmpty() ? "\t; " + hint : "");
	}

	public static void print(final Prototype proto, final PrintWriter out, boolean isMain) {
		Check.notNull(proto);
		Check.notNull(out);

		IntVector code = proto.getCode();
		ReadOnlyArray<Object> constants = proto.getConstants();
		ReadOnlyArray<Prototype.LocalVariable> locals = proto.getLocalVariables();
		ReadOnlyArray<Prototype.UpvalueDesc> upvalues = proto.getUpValueDescriptions();
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

			int line = proto.getLineAtPC(i);
			out.print("[" + (line >= 0 ? line : "-") + "]");  // line number

			out.print('\t');

			out.println(instructionInfoWithHints(proto, i));
		}

		// constants
		out.println("constants (" + constants.size() + ") for " + pseudoAddr(proto) + ":");
		for (int i = 0; i < constants.size(); i++) {
			out.print('\t');
			out.print(i + 1);
			out.print('\t');
			out.println(constantToString(constants, i));
		}

		// locals
		out.println("locals (" + locals.size() + ") for " + pseudoAddr(proto) + ":");
		for (int i = 0; i < locals.size(); i++) {
			out.print('\t');
			out.print(i);  // index
			out.print('\t');

			Prototype.LocalVariable lv = locals.get(i);
			out.print(lv.name + "\t" + (lv.beginPC + 1) + "\t" + (lv.endPC + 1));
			out.println();
		}

		// upvalues
		out.println("upvalues (" + upvalues.size() + ") for " + pseudoAddr(proto) + ":");
		for (int i = 0; i < upvalues.size(); i++) {
			out.print('\t');
			out.print(i);  // index
			out.print('\t');

			Prototype.UpvalueDesc uvd = upvalues.get(i);
			out.print(uvd.name + "\t" + (uvd.inStack ? 1 : 0) + "\t" + uvd.index);
			out.println();
		}

		// nested prototypes
		for (Prototype np : nested) {
			out.println();
			print(np, out, false);
		}

		out.flush();
	}

}

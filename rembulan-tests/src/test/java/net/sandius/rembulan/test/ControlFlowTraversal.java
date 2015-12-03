package net.sandius.rembulan.test;

import net.sandius.rembulan.core.OpCode;
import net.sandius.rembulan.core.Prototype;
import net.sandius.rembulan.core.PrototypePrinter;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.io.PrintStream;

public class ControlFlowTraversal {

	public final Prototype prototype;

	public final int[][] next;
	public final int[][] prev;

	public ControlFlowTraversal(Prototype prototype) {
		Check.notNull(prototype);

		this.prototype = prototype;

		IntVector code = prototype.getCode();

		prev = new int[code.length()][];
		next = new int[code.length()][];

		for (int i = 0; i < code.length(); i++) {
			prev[i] = new int[0];
			next[i] = new int[0];
		}

		traverse(-1, 0);
		for (int i = 0; i < code.length(); i++) {
			visit(i);
		}
	}

	private int[] append(int[] a, int i) {
		Check.notNull(a);
		assert (!contains(a, i));

		int[] result = new int[a.length + 1];
		System.arraycopy(a, 0, result, 0, a.length);
		result[a.length] = i;
		return result;
	}

	private boolean contains(int[] a, int i) {
		for (int elem : a) {
			if (elem == i) return true;
		}
		return false;
	}

	private void traverse(int from, int to) {
		IntVector code = prototype.getCode();

		if (from >= 0) {
			next[from] = append(next[from], to);
		}
		if (to >= 0) {
			prev[to] = append(prev[to], from);
		}
	}

	private void visit(int pc) {
		int insn = prototype.getCode().get(pc);

		int oc = OpCode.opCode(insn);
		int a = OpCode.arg_A(insn);
		int b = OpCode.arg_B(insn);
		int c = OpCode.arg_C(insn);
		int bx = OpCode.arg_Bx(insn);
		int sbx = OpCode.arg_sBx(insn);
		int ax = OpCode.arg_Ax(insn);

		switch (oc) {
			case OpCode.MOVE:
			case OpCode.LOADK:
			case OpCode.LOADNIL:
			case OpCode.GETUPVAL: 
			case OpCode.GETTABUP: 
			case OpCode.GETTABLE: 
			case OpCode.SETTABUP: 
			case OpCode.SETUPVAL: 
			case OpCode.SETTABLE: 
			case OpCode.NEWTABLE: 
			case OpCode.SELF:     
			case OpCode.ADD:
			case OpCode.SUB:
			case OpCode.MUL:
			case OpCode.MOD:
			case OpCode.POW:
			case OpCode.DIV:
			case OpCode.IDIV:
			case OpCode.BAND:
			case OpCode.BOR:
			case OpCode.BXOR:
			case OpCode.SHL:
			case OpCode.SHR:
			case OpCode.UNM:
			case OpCode.BNOT:
			case OpCode.NOT:
			case OpCode.LEN:
			case OpCode.CONCAT:
				traverse(pc, pc + 1); break;

//			//case OpCode.LOADKX:   ie.l_LOADKX(extra);  break;

			case OpCode.LOADBOOL:
				if (c != 0) traverse(pc, pc + 2); else traverse(pc, pc + 1); break;

			case OpCode.JMP:
				traverse(pc, pc + sbx + 1); break;
			
			case OpCode.EQ:   
			case OpCode.LT:   
			case OpCode.LE:   
			case OpCode.TEST:     
			case OpCode.TESTSET:
				traverse(pc, pc + 1); traverse(pc, pc + 2); break;

			case OpCode.CALL:
				traverse(pc, pc + 1); break;

			case OpCode.TAILCALL:
			case OpCode.RETURN:
				traverse(pc, -1); break;

			case OpCode.FORLOOP:
				traverse(pc, pc + sbx + 1); traverse(pc, pc + 1); break;

			case OpCode.FORPREP:
				traverse(pc, pc + sbx + 1); break;

			case OpCode.TFORCALL:
				traverse(pc, pc + 1); break;

			case OpCode.TFORLOOP:
				traverse(pc, pc + sbx + 1); traverse(pc, pc + 1); break;

			case OpCode.SETLIST:
				traverse(pc, pc + 1); break;

			case OpCode.CLOSURE:
				traverse(pc, pc + 1); break;

			case OpCode.VARARG:
				traverse(pc, pc + 1); break;

//			case OpCode.EXTRAARG:  

			default: throw new UnsupportedOperationException("Unsupported opcode: " + oc);
		}

	}

	private boolean skip(int pc) {
		int[] thisPrev = prev[pc];
		if (thisPrev.length == 0) return true;

		for (int p : thisPrev) {
			if (p == -1 || !skip(p)) return false;
		}

		return true;
	}

	public void print(PrintStream out) {
		IntVector code = prototype.getCode();

		for (int i = 0; i < code.length(); i++) {
			out.print("\t");
			out.print(i + 1);
			out.print("\t");

			if (!skip(i)) {
				int[] thisPrev = prev[i];
				int[] thisNext = next[i];

				out.print("[");
				for (int j = 0; j < thisPrev.length; j++) {
					int pc = thisPrev[j] + 1;
					out.print(pc != 0 ? pc : "-");
					if (j + 1 < thisPrev.length) {
						out.print(" ");
					}
				}
				out.print("] --> [");
				for (int j = 0; j < thisNext.length; j++) {
					int pc = thisNext[j] + 1;
					out.print(pc != 0 ? pc : "*");
					if (j + 1 < thisNext.length) {
						out.print(" ");
					}
				}
				out.print("]");
			}
			else {
				out.print("(skipped)");
			}

			out.print("\t");

			out.print(PrototypePrinter.instructionInfo(prototype, i));
			out.println();
		}
	}


}

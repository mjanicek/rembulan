package net.sandius.rembulan.test;

import net.sandius.rembulan.core.OpCode;
import net.sandius.rembulan.core.Prototype;
import net.sandius.rembulan.core.PrototypePrinter;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntBuffer;
import net.sandius.rembulan.util.IntVector;

import java.io.PrintStream;
import java.util.ArrayList;

public class ControlFlowTraversal {

	public final Prototype prototype;

	private final ArrayList<Block> blocks;

	public ControlFlowTraversal(Prototype prototype) {
		Check.notNull(prototype);

		this.prototype = prototype;
		this.blocks = analyseBlocks();
	}

	private static void traverse(IntBuffer[] prev, IntBuffer[] next, int from, int to) {
		if (from >= 0) {
			next[from].append(to);
		}
		if (to >= 0) {
			prev[to].append(from);
		}
	}

	private void visit(IntBuffer[] prev, IntBuffer[] next, int pc) {
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
				traverse(prev, next, pc, pc + 1); break;

//			//case OpCode.LOADKX:   ie.l_LOADKX(extra);  break;

			case OpCode.LOADBOOL:
				if (c != 0) traverse(prev, next, pc, pc + 2); else traverse(prev, next, pc, pc + 1); break;

			case OpCode.JMP:
				traverse(prev, next, pc, pc + sbx + 1); break;

			case OpCode.EQ:
			case OpCode.LT:
			case OpCode.LE:
			case OpCode.TEST:
			case OpCode.TESTSET:
				traverse(prev, next, pc, pc + 1); traverse(prev, next, pc, pc + 2); break;

			case OpCode.CALL:
				traverse(prev, next, pc, pc + 1); break;

			case OpCode.TAILCALL:
			case OpCode.RETURN:
				traverse(prev, next, pc, -1); break;

			case OpCode.FORLOOP:
				traverse(prev, next, pc, pc + sbx + 1); traverse(prev, next, pc, pc + 1); break;

			case OpCode.FORPREP:
				traverse(prev, next, pc, pc + sbx + 1); break;

			case OpCode.TFORCALL:
				traverse(prev, next, pc, pc + 1); break;

			case OpCode.TFORLOOP:
				traverse(prev, next, pc, pc + sbx + 1); traverse(prev, next, pc, pc + 1); break;

			case OpCode.SETLIST:
				traverse(prev, next, pc, pc + 1); break;

			case OpCode.CLOSURE:
				traverse(prev, next, pc, pc + 1); break;

			case OpCode.VARARG:
				traverse(prev, next, pc, pc + 1); break;

//			case OpCode.EXTRAARG:

			default: throw new UnsupportedOperationException("Unsupported opcode: " + oc);
		}

	}

	public ArrayList<Block> analyseBlocks() {
		IntVector code = prototype.getCode();

		IntBuffer[] prev = new IntBuffer[code.length()];
		IntBuffer[] next = new IntBuffer[code.length()];

		for (int i = 0; i < code.length(); i++) {
			prev[i] = new IntBuffer();
			next[i] = new IntBuffer();
		}

		traverse(prev, next, -1, 0);
		for (int i = 0; i < code.length(); i++) {
			visit(prev, next, i);
		}

		ArrayList<Block> blocks = new ArrayList<>();

		for (int i = 0; i < code.length(); i++) {
			IntBuffer instrs = IntBuffer.of(i);
			blocks.add(new Block(instrs, prev[i], next[i]));
		}

		int i = 0;
		while (i < blocks.size()) {
			Block blk_i = blocks.get(i);

			// merge with next?
			if (blk_i.next.length() == 1) {
				int j = blk_i.next.get(0);
				if (j >= 0) {
					Block blk_j = blocks.get(j);
					if (blk_j.prev.length() == 1) {
						assert (blk_j.prev.get(0) == i);

						blk_i.merge(blk_j);

						blocks.remove(j);

						for (Block blk : blocks) {
							blk.renumber(j, i);
							blk.shift(j);
						}

						i = 0;
						continue;
					}
				}
			}

			// skip?
			if (blk_i.prev.length() == 0) {

				blocks.remove(i);

				for (Block blk : blocks) {
					blk.erase(i);
					blk.shift(i);
				}

				i = 0;
				continue;
			}

			i++;
		}

		return blocks;
	}

	public void print(PrintStream out) {
//		out.println("Blocks (" + blocks.length + " total):");

		for (int i = 0; i < blocks.size(); i++) {
			out.print("\t");
			out.print("#");
			out.print(i);

			out.print(":");

			Block b = blocks.get(i);

			out.print(" ");

			out.print("[");
			for (int j = 0; j < b.prev.length(); j++) {
				int idx = b.prev.get(j);
				out.print(idx >= 0 ? "#" + idx : "-");
				if (j + 1 < b.prev.length()) out.print(" ");
			}
			out.print("]");
			out.print(" --> ");
			out.print("[");
			for (int j = 0; j < b.next.length(); j++) {
				int idx = b.next.get(j);
				out.print(idx >= 0 ? "#" + idx : "*");
				if (j + 1 < b.next.length()) out.print(" ");
			}
			out.print("]");

			out.println();

			for (int j = 0; j < b.instructionIndices.length(); j++) {
				out.print("\t\t");
				int pc = b.instructionIndices.get(j);
				out.print(pc + 1);
				out.print("\t");
				out.print(PrototypePrinter.instructionInfo(prototype, pc));
				out.println();
			}
		}
	}

	public static class Block {

		public final IntBuffer instructionIndices;
		public final IntBuffer prev;
		public final IntBuffer next;

		public Block(IntBuffer instructionIndices, IntBuffer prev, IntBuffer next) {
			Check.notNull(instructionIndices);
			Check.notNull(prev);
			Check.notNull(next);

			this.instructionIndices = instructionIndices;
			this.prev = prev;
			this.next = next;
		}

		public void merge(Block that) {
			instructionIndices.append(that.instructionIndices);
			next.clear();
			next.append(that.next);
		}

		public void shift(int idx) {
			for (int j = 0; j < prev.length(); j++) {
				int tgt = prev.get(j);
				assert (tgt != idx);
				prev.set(j, tgt > idx ? tgt - 1 : tgt);
			}

			for (int j = 0; j < next.length(); j++) {
				int tgt = next.get(j);
				assert (tgt != idx);
				next.set(j, tgt > idx ? tgt - 1 : tgt);
			}
		}

		public void renumber(int from, int to) {
			prev.replaceValue(from, to);
			next.replaceValue(from, to);
		}

		public void erase(int idx) {
			prev.removeValue(idx);
			next.removeValue(idx);
		}

	}

}

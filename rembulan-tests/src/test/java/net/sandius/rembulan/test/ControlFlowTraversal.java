package net.sandius.rembulan.test;

import net.sandius.rembulan.core.OpCode;
import net.sandius.rembulan.core.Prototype;
import net.sandius.rembulan.core.PrototypePrinter;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntVector;

import java.io.PrintStream;

public class ControlFlowTraversal {

	public final Prototype prototype;

	private final Block[] blocks;

	public ControlFlowTraversal(Prototype prototype) {
		Check.notNull(prototype);

		this.prototype = prototype;
		this.blocks = analyseBlocks();
	}

	private int[] cpy(int[] a) {
		Check.notNull(a);
		int[] result = new int[a.length];
		System.arraycopy(a, 0, result, 0, a.length);
		return result;
	}

	private int[] append(int[] a, int i) {
		Check.notNull(a);
		assert (!contains(a, i));

		int[] result = new int[a.length + 1];
		System.arraycopy(a, 0, result, 0, a.length);
		result[a.length] = i;
		return result;
	}

	private int[] remove(int[] array, int value) {
		if (contains(array, value)) {
			int idx = 0;
			for (int elem : array) {
				if (elem == value) break;
				idx++;
			}

			assert (array[idx] == value);

			int[] result = new int[array.length - 1];
			System.arraycopy(array, 0, result, 0, idx);
			System.arraycopy(array, idx + 1, result, idx, array.length - idx - 1);
			return result;
		}
		else {
			return array;
		}
	}

	private int[] replace(int[] array, int old, int nu) {
		Check.notNull(array);

		for (int i = 0; i < array.length; i++) {
			if (array[i] == old) {
				array[i] = nu;
			}
		}

		return array;
	}

	private boolean contains(int[] a, int i) {
		for (int elem : a) {
			if (elem == i) return true;
		}
		return false;
	}

	private int[] concat(int[] a, int[] b) {
		Check.notNull(a);
		Check.notNull(b);

		int[] result = new int[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	private String tostring(int[] array) {
		Check.notNull(array);

		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			buf.append(array[i]);
			if (i + 1 < array.length) {
				buf.append(" ");
			}
		}
		return buf.toString();
	}

	private void traverse(int[][] prev, int[][] next, int from, int to) {
		IntVector code = prototype.getCode();

		if (from >= 0) {
			next[from] = append(next[from], to);
		}
		if (to >= 0) {
			prev[to] = append(prev[to], from);
		}
	}

	private void visit(int[][] prev, int[][] next, int pc) {
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

	private Block[] shiftIndices(Block[] blocks, int idx) {
//		System.out.println("\t\tshifting blocks above " + idx);
		for (int i = 0; i < blocks.length; i++) {
			Block blk = blocks[i];
			if (blk != null) {
				int[] prev = blk.prev;
				for (int j = 0; j < prev.length; j++) {
					int tgt = prev[j];
					assert (tgt != idx);
					prev[j] = tgt > idx ? tgt - 1 : tgt;
				}
				blk.prev = prev;

				int[] next = blk.next;
				for (int j = 0; j < next.length; j++) {
					int tgt = next[j];
					assert (tgt != idx);
					next[j] = tgt > idx ? tgt - 1 : tgt;
				}
				blk.next = next;
			}
		}

		return blocks;
	}

	private Block[] skipIndex(Block[] blocks, int idx) {
//		System.out.println("\t\tskipping index " + idx);
		Check.notNull(blocks);
		Check.inRange(idx, 0, blocks.length - 1);

		Block[] result = new Block[blocks.length - 1];
		System.arraycopy(blocks, 0, result, 0, idx);
		System.arraycopy(blocks, idx + 1, result, idx, blocks.length - idx - 1);
		return result;
	}

	// merge B into A (A remains)
	private Block[] mergeBlocks(Block[] blocks, int a, int b) {
//		System.out.println("merging block #" + b + " into #" + a);
//		System.out.println("\tA (" + a + ") before = { prev = [" + tostring(blocks[a].prev) + "], next = [" + tostring(blocks[a].next) + "] }");
//		System.out.println("\tB (" + b + ") before = { prev = [" + tostring(blocks[b].prev) + "], next = [" + tostring(blocks[b].next) + "] }");

		Block b_a = blocks[a];
		Block b_b = blocks[b];

		assert (b_a.next.length == 1);
		assert (b_b.prev.length == 1);
		assert (b_a.next[0] == b);
		assert (b_b.prev[0] == a);

		blocks[a] = new Block(
				concat(b_a.instructionIndices, b_b.instructionIndices),
				cpy(b_a.prev),
				cpy(b_b.next)
		);

		for (int i = 0; i < blocks.length; i++) {
			if (i != a && i != b) {
				Block blk = blocks[i];
				int[] prev = blk.prev;
				int[] next = blk.next;

				assert (!contains(next, b));

				if (contains(prev, b)) {
					blk.prev = replace(blk.prev, b, a);
				}
			}
		}

		blocks[b] = null;

		blocks = shiftIndices(blocks, b);
		blocks = skipIndex(blocks, b);

//		System.out.println("\tA (" + a + ") after =  { prev = [" + tostring(blocks[a].prev) + "], next = [" + tostring(blocks[a].next) + "] }");

		return blocks;
	}

	private Block[] skipBlock(Block[] blocks, int idx) {
//		System.out.println("skipping block #" + idx);

		assert (blocks[idx].prev.length == 0);

		// remove idx from links
		int[] nxt = blocks[idx].next;

//		System.out.println("\tnext = [" + tostring(nxt) + "]");

		for (int i = 0; i < nxt.length; i++) {
			if (nxt[i] >= 0) {
//				System.out.println("\t\tlooking at #" + nxt[i]);
				Block blk = blocks[nxt[i]];
//				System.out.println("\t\t\tprev before = [" + tostring(blk.prev) + "]");

				assert (contains(blk.prev, idx));

				blk.prev = remove(blk.prev, idx);
//				System.out.println("\t\t\tprev after  = [" + tostring(blk.prev) + "]");
			}
		}

		blocks[idx] = null;

		blocks = shiftIndices(blocks, idx);
		blocks = skipIndex(blocks, idx);

		return blocks;
	}

	public Block[] analyseBlocks() {
		IntVector code = prototype.getCode();

		int[][] prev = new int[code.length()][];
		int[][] next = new int[code.length()][];

		for (int i = 0; i < code.length(); i++) {
			prev[i] = new int[0];
			next[i] = new int[0];
		}

		traverse(prev, next, -1, 0);
		for (int i = 0; i < code.length(); i++) {
			visit(prev, next, i);
		}

		Block[] blocks = new Block[code.length()];

		for (int i = 0; i < code.length(); i++) {
			blocks[i] = new Block(new int[] { i }, prev[i], next[i]);
		}

		int i = 0;
		while (i < blocks.length) {
			Block blk = blocks[i];

			// merge with next?
			if (blk.next.length == 1) {
				int j = blk.next[0];
				if (j >= 0) {
					Block b = blocks[j];
					if (b.prev.length == 1) {
						assert (b.prev[0] == i);
						blocks = mergeBlocks(blocks, i, j);
						i = 0;
						continue;
					}
				}
			}

			// skip?
			if (blk.prev.length == 0) {
				blocks = skipBlock(blocks, i);
				i = 0;
				continue;
			}

			i++;
		}

		return blocks;
	}

	public void print(PrintStream out) {
//		out.println("Blocks (" + blocks.length + " total):");

		for (int i = 0; i < blocks.length; i++) {
			out.print("\t");
			out.print("#");
			out.print(i);

			out.print(":");

			Block b = blocks[i];

			out.print(" ");

			out.print("[");
			for (int j = 0; j < b.prev.length; j++) {
				int idx = b.prev[j];
				out.print(idx >= 0 ? "#" + idx : "-");
				if (j + 1 < b.prev.length) out.print(" ");
			}
			out.print("]");
			out.print(" --> ");
			out.print("[");
			for (int j = 0; j < b.next.length; j++) {
				int idx = b.next[j];
				out.print(idx >= 0 ? "#" + idx : "*");
				if (j + 1 < b.next.length) out.print(" ");
			}
			out.print("]");

			out.println();

			for (int j = 0; j < b.instructionIndices.length; j++) {
				out.print("\t\t");
				int pc = b.instructionIndices[j];
				out.print(pc + 1);
				out.print("\t");
				out.print(PrototypePrinter.instructionInfo(prototype, pc));
				out.println();
			}
		}
	}

	public static class Block {

		public int[] instructionIndices;
		public int[] prev;
		public int[] next;

		public Block(int[] instructionIndices, int[] prev, int[] next) {
			this.instructionIndices = instructionIndices;
			this.prev = prev;
			this.next = next;
		}

	}

}

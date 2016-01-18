package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.OpCode;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.lbc.PrototypePrinter;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.IntBuffer;
import net.sandius.rembulan.util.IntVector;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
			blocks.add(Block.newBlock(i + 1, code.get(i), prototype.getMaximumStackSize(), prev[i], next[i]));
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

	private String slotState(int pc) {
		StringBuilder bld = new StringBuilder();

		ReadOnlyArray<Prototype.LocalVariable> locals = prototype.getLocalVariables();

//		int numActive = 0;
//		for (Prototype.LocalVariable lv : locals) {
//			if (pc >= lv.beginPC && pc <= lv.endPC) {
//				numActive += 1;
//			}
//		}
//
//		bld.append(numActive + ": ");
		bld.append("[ ");

		int lidx = 0;

		for (int i = 0; i < prototype.getMaximumStackSize(); i++) {
			String sl = null;

			while (lidx < locals.size()) {
				Prototype.LocalVariable lv = locals.get(lidx);
				lidx += 1;
				if (pc >= lv.beginPC && pc < lv.endPC) {
					sl = Integer.toString(lidx - 1);
					break;
				}
			}

			bld.append('-');
//			if (sl != null) {
//				bld.append(sl);
//			}
//			else {
//				bld.append('-');
//			}

			if (i + 1 < prototype.getMaximumStackSize()) bld.append(' ');
		}

//		for (int j = 0; j < locals.size(); j++) {
//			Prototype.LocalVariable lv = locals.get(j);
//			if (pc >= lv.beginPC && pc <= lv.endPC) {
//				bld.append(j);
//			}
//			else {
//				bld.append('-');
//			}
//
//			if (j + 1 < locals.size()) bld.append('|');
//		}

		bld.append(" ]");
		return bld.toString();
	}

	private String closureHint(int insn) {
		StringBuilder bld = new StringBuilder();

		Prototype closureProto = prototype.getNestedPrototypes().get(OpCode.arg_Bx(insn));

		ReadOnlyArray<Prototype.UpvalueDesc> upvals = closureProto.getUpValueDescriptions();
		for (Prototype.UpvalueDesc uvd : upvals) {
			bld.append(uvd.index);
			if (!uvd.inStack) bld.append('^');
			bld.append(' ');
		}

		return bld.toString();
	}

	private static void appendHintDelimiter(StringBuilder bld) {
		if (bld.length() > 0) {
			bld.append("; ");
		}
	}

	public String blockToString(Block b, String prefix, boolean slots, boolean hints) {
		StringBuilder bld = new StringBuilder();
		
		for (int j = 0; j < b.nodes.size(); j++) {
			bld.append(prefix);
			BlockNode node = b.nodes.get(j);

			int pc = node.getPc();

			bld.append(pc > 0 ? pc : "-");
			bld.append('\t');

			if (slots) {
				bld.append("[ ").append(b.slots()).append(" ]");
//				bld.append(slotState(pc - 1));
				bld.append('\t');
			}

//			int insn = prototype.getCode().get(pc);
//			bld.append(pc + 1);
//			bld.append("\t");

			bld.append(node instanceof Instruction && ((Instruction) node).canTransferControl() ? "*" : " ");
//				bld.append("\t");
			bld.append(" ");
			bld.append(node.toString());

			if (node instanceof Instruction) {
				int opcode = ((Instruction) node).getOpCode();
				int insn = ((Instruction) node).insn;

				StringBuilder hint = new StringBuilder();

				if (hints) {
					String hs = PrototypePrinter.instructionInfoHints(insn, pc, prototype.getConstants(), prototype.getNestedPrototypes());
					if (!hs.isEmpty()) {
						hint.append(hs);
					}
				}

				if (opcode == OpCode.CLOSURE) {
					appendHintDelimiter(hint);
					hint.append("capture ").append(closureHint(((Instruction) node).insn));
				}
				if (opcode == OpCode.JMP) {
					int a = OpCode.arg_A(insn);

					if (a != 0) {
						appendHintDelimiter(hint);
						int min = a - 1;
						hint.append("freshen regs >= ").append(min);
					}
				}

				if (hint.length() > 0) {
					bld.append("\t; ").append(hint);
				}
			}

			bld.append('\n');
		}

		return bld.toString();
	}

	public void print(PrintStream out) {
		for (int i = 0; i < blocks.size(); i++) {
			out.print("\t");
			out.print("#");
			out.print(i);

			out.print(":");

			Block b = blocks.get(i);

			out.print(" ");

			out.print("(cost $" + b.getCost() + ")");

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
			
			String bk = blockToString(b, "\t\t", true, true);
			out.print(bk);

			if (i + 1 < blocks.size()) {
				out.println();
			}
		}
	}
	
	public List<Block> getBlocks() {
		return Collections.unmodifiableList(blocks);
	}

}

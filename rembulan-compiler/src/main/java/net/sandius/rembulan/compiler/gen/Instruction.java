package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.OpCode;
import net.sandius.rembulan.lbc.PrototypePrinter;

public abstract class Instruction extends BlockNode {

	protected final int pc;
	protected final int insn;

	public Instruction(int pc, int insn) {
		this.pc = pc;
		this.insn = insn;
	}

	public int getOpCode() {
		return OpCode.opCode(insn);
	}

	@Override
	public int getPc() {
		return pc;
	}

	@Override
	public int getCost() {
		return 1;
	}

	public abstract boolean canTransferControl();

	@Override
	public String toString() {
		return PrototypePrinter.instructionInfo(insn);
	}

	public static Instruction valueOf(int pc, int insn) {
		int oc = OpCode.opCode(insn);

		Instruction i = UnconditionalInstruction.fromInstruction(pc, insn);
		if (i == null) {
			i = BranchInstruction.fromInstruction(pc, insn);
		}
		if (i == null) {
			i = TerminalInstruction.fromInstruction(pc, insn);
		}
		if (i == null) {
			throw new IllegalArgumentException("Unsupported instruction: " + insn + " (opcode = " + oc + ")");
		}

		return i;
	}

}

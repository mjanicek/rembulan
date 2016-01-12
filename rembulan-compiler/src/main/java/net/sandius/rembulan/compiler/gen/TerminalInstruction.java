package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.OpCode;

public class TerminalInstruction extends Instruction {

	public TerminalInstruction(int pc, int insn) {
		super(pc, insn);
	}

	public static TerminalInstruction fromInstruction(int pc, int insn) {
		int opcode = OpCode.opCode(insn);

		switch (opcode) {
			case OpCode.TAILCALL:
			case OpCode.RETURN:
				return new TerminalInstruction(pc, insn);

			default:
				return null;
		}
	}

	@Override
	public boolean canTransferControl() {
		int opcode = getOpCode();

		switch (opcode) {
			case OpCode.TAILCALL:
				return true;
			case OpCode.RETURN:
				return false;
			default:
				throw new IllegalStateException("Illegal opcode: " + opcode);
		}
	}

}

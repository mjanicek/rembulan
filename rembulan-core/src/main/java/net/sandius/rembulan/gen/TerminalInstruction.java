package net.sandius.rembulan.gen;

import net.sandius.rembulan.core.OpCode;

public class TerminalInstruction extends Instruction {

	public TerminalInstruction(int insn) {
		super(insn);
	}

	public static TerminalInstruction fromInstruction(int insn) {
		int opcode = OpCode.opCode(insn);

		switch (opcode) {
			case OpCode.TAILCALL:
			case OpCode.RETURN:
				return new TerminalInstruction(insn);

			default:
				return null;
		}
	}

}

package net.sandius.rembulan.gen;

import net.sandius.rembulan.core.OpCode;

public class BranchInstruction extends Instruction {

	protected final int left;
	protected final int right;

	private BranchInstruction(int insn, int left, int right) {
		super(insn);
		this.left = left;
		this.right = right;
	}

	public static BranchInstruction fromInstruction(int insn) {
		int opcode = OpCode.opCode(insn);
		int sbx = OpCode.arg_sBx(insn);

		final int left;
		final int right;

		switch (opcode) {
			case OpCode.EQ:
			case OpCode.LT:
			case OpCode.LE:
			case OpCode.TEST:
			case OpCode.TESTSET:
				left = 1;
				right = 2;
				break;

			case OpCode.FORLOOP:
			case OpCode.TFORLOOP:
				left = sbx + 1;
				right = 1;
				break;

			default:
				return null;
		}

		return new BranchInstruction(insn, left, right);
	}

	public int leftOffset() {
		return left;
	}

	public int rightOffset() {
		return right;
	}

	@Override
	public boolean canTransferControl() {
		int opcode = getOpCode();

		switch (opcode) {
			case OpCode.EQ:
			case OpCode.LT:
			case OpCode.LE:
				return true;

			case OpCode.TEST:
			case OpCode.TESTSET:
				return false;

			case OpCode.FORLOOP:
			case OpCode.TFORLOOP:
				return false;

			default:
				throw new IllegalStateException("Illegal opcode: " + opcode);
		}
	}

}

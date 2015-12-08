package net.sandius.rembulan.gen;

import net.sandius.rembulan.core.OpCode;

public class UnconditionalInstruction extends Instruction {

	protected final int next;

	private UnconditionalInstruction(int insn, int next) {
		super(insn);
		this.next = next;
	}

	public static UnconditionalInstruction fromInstruction(int insn) {
		int opcode = OpCode.opCode(insn);
		int c = OpCode.arg_C(insn);
		int sbx = OpCode.arg_sBx(insn);

		final int next;

		switch (opcode) {

			case OpCode.MOVE:
			case OpCode.LOADK:
//			//case OpCode.LOADKX:   ie.l_LOADKX(extra);  break;
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
			case OpCode.JMP:
				next = 1;
				break;

			case OpCode.LOADBOOL:
				next = c != 0 ? 2 : 1;
				break;

			case OpCode.CALL:
				next = 1;
				break;

			case OpCode.FORPREP:
				next = sbx + 1;
				break;

			case OpCode.TFORCALL:
				next = 1;
				break;

			case OpCode.SETLIST:
			case OpCode.CLOSURE:
			case OpCode.VARARG:
				next = 1;
				break;

			default:
				return null;
		}

		return new UnconditionalInstruction(insn, next);
	}

	public int nextOffset() {
		return next;
	}

	@Override
	public boolean canTransferControl() {
		int opcode = getOpCode();

		switch (opcode) {
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
				return true;

			case OpCode.CALL:
				return true;

			case OpCode.FORPREP:
				return false;

			case OpCode.TFORCALL:
				return true;

			default:
				return false;
		}
	}

}

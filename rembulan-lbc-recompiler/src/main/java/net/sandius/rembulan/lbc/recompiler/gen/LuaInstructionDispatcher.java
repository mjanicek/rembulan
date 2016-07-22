package net.sandius.rembulan.lbc.recompiler.gen;

import net.sandius.rembulan.lbc.OpCode;

public class LuaInstructionDispatcher {

	private final LuaInstructionVisitor ie;

	public LuaInstructionDispatcher(LuaInstructionVisitor visitor) {
		this.ie = visitor;
	}

	public void dispatch(int insn) {
		int oc = OpCode.opCode(insn);

		int a = OpCode.arg_A(insn);
		int b = OpCode.arg_B(insn);
		int c = OpCode.arg_C(insn);
		int ax = OpCode.arg_Ax(insn);
		int bx = OpCode.arg_Bx(insn);
		int sbx = OpCode.arg_sBx(insn);

		switch (oc) {
			case OpCode.MOVE:     ie.l_MOVE(a, b); break;
			case OpCode.LOADK:    ie.l_LOADK(a, bx); break;
			//case OpCode.LOADKX:   ie.l_LOADKX(extra);  break;
			case OpCode.LOADBOOL: ie.l_LOADBOOL(a, b, c); break;
			case OpCode.LOADNIL:  ie.l_LOADNIL(a, b); break;
			case OpCode.GETUPVAL: ie.l_GETUPVAL(a, b); break;
			case OpCode.GETTABUP: ie.l_GETTABUP(a, b, c); break;
			case OpCode.GETTABLE: ie.l_GETTABLE(a, b, c); break;
			case OpCode.SETTABUP: ie.l_SETTABUP(a, b, c); break;
			case OpCode.SETUPVAL: ie.l_SETUPVAL(a, b); break;
			case OpCode.SETTABLE: ie.l_SETTABLE(a, b, c); break;
			case OpCode.NEWTABLE: ie.l_NEWTABLE(a, b, c); break;
			case OpCode.SELF:     ie.l_SELF(a, b, c); break;

			case OpCode.ADD:   ie.l_ADD(a, b, c); break;
			case OpCode.SUB:   ie.l_SUB(a, b, c); break;
			case OpCode.MUL:   ie.l_MUL(a, b, c); break;
			case OpCode.MOD:   ie.l_MOD(a, b, c); break;
			case OpCode.POW:   ie.l_POW(a, b, c); break;
			case OpCode.DIV:   ie.l_DIV(a, b, c); break;
			case OpCode.IDIV:  ie.l_IDIV(a, b, c); break;
			case OpCode.BAND:  ie.l_BAND(a, b, c); break;
			case OpCode.BOR:   ie.l_BOR(a, b, c); break;
			case OpCode.BXOR:  ie.l_BXOR(a, b, c); break;
			case OpCode.SHL:   ie.l_SHL(a, b, c); break;
			case OpCode.SHR:   ie.l_SHR(a, b, c); break;

			case OpCode.UNM:   ie.l_UNM(a, b); break;
			case OpCode.BNOT:  ie.l_BNOT(a, b); break;
			case OpCode.NOT:   ie.l_NOT(a, b); break;
			case OpCode.LEN:   ie.l_LEN(a, b); break;

			case OpCode.CONCAT:  ie.l_CONCAT(a, b, c); break;

			case OpCode.JMP:  ie.l_JMP(a, sbx); break;
			case OpCode.EQ:   ie.l_EQ(a, b, c); break;
			case OpCode.LT:   ie.l_LT(a, b, c); break;
			case OpCode.LE:   ie.l_LE(a, b, c); break;

			case OpCode.TEST:     ie.l_TEST(a, c); break;
			case OpCode.TESTSET:  ie.l_TESTSET(a, b, c); break;

			case OpCode.CALL:      ie.l_CALL(a, b, c); break;
			case OpCode.TAILCALL:  ie.l_TAILCALL(a, b, c); break;
			case OpCode.RETURN:    ie.l_RETURN(a, b); break;

			case OpCode.FORLOOP:  ie.l_FORLOOP(a, sbx); break;
			case OpCode.FORPREP:  ie.l_FORPREP(a, sbx); break;

			case OpCode.TFORCALL:  ie.l_TFORCALL(a, c); break;
			case OpCode.TFORLOOP:  ie.l_TFORLOOP(a, sbx); break;

			case OpCode.SETLIST:  ie.l_SETLIST(a, b, c); break;

			case OpCode.CLOSURE:  ie.l_CLOSURE(a, bx); break;

			case OpCode.VARARG:  ie.l_VARARG(a, b); break;

			case OpCode.EXTRAARG:  ie.l_EXTRAARG(ax); break;

			default: throw new UnsupportedOperationException("Unsupported opcode: " + oc);
		}
	}

}

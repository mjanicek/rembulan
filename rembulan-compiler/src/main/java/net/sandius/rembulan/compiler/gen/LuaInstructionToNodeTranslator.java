package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.AccountingNode;
import net.sandius.rembulan.compiler.gen.block.CloseUpvalues;
import net.sandius.rembulan.compiler.gen.block.LineInfo;
import net.sandius.rembulan.compiler.gen.block.NodeAppender;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.lbc.OpCode;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;

import static net.sandius.rembulan.compiler.gen.block.LuaInstruction.*;

public class LuaInstructionToNodeTranslator {

	private static int registerOrConst(int i) {
		return OpCode.isK(i) ? -1 - OpCode.indexK(i) : i;
	}

	public void translate(Prototype prototype, int insn, int pc, int line, ReadOnlyArray<Target> pcToLabel) {
		int opcode = OpCode.opCode(insn);
		int a = OpCode.arg_A(insn);
		int b = OpCode.arg_B(insn);
		int bx = OpCode.arg_Bx(insn);
		int c = OpCode.arg_C(insn);
		int sbx = OpCode.arg_sBx(insn);

		NodeAppender tail = new NodeAppender(pcToLabel.get(pc));

		// prefix
		tail.append(new LineInfo(line)).append(new AccountingNode.TickBefore());

		switch (opcode) {
			case OpCode.MOVE:
				tail.append(new Move(a, b)).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.LOADK:
				tail.append(new LoadK(prototype, a, bx)).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.LOADNIL:
				tail.append(new LoadNil(a, b)).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.LOADBOOL:
				tail.append(new LoadBool(a, b)).jumpTo(pcToLabel.get(pc + (c != 0 ? 2 : 1)));
				break;

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
				tail.append(new BinOp(
						prototype,
						BinOpType.fromOpcode(opcode),
						a,
						registerOrConst(b),
						registerOrConst(c))).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.UNM:
			case OpCode.BNOT:
			case OpCode.NOT:
			case OpCode.LEN:
				tail.append(new UnOp(
						prototype,
						UnOpType.fromOpcode(opcode),
						a,
						b)).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.CONCAT:
				tail.append(new Concat(a, b, c)).jumpTo(pcToLabel.get(pc + 1));
				break;


			case OpCode.JMP:
				if (a > 0) {
					tail.append(new CloseUpvalues(a - 1));
				}
				tail.jumpTo(pcToLabel.get(pc + sbx + 1));
				break;

			case OpCode.EQ:
				tail.branch(new Eq(
						pcToLabel.get(pc + 1),
						pcToLabel.get(pc + 2),
						a == 0,
						registerOrConst(b),
						registerOrConst(c)));
				break;

			case OpCode.LT:
				tail.branch(new Lt(
						pcToLabel.get(pc + 1),
						pcToLabel.get(pc + 2),
						a == 0,
						registerOrConst(b),
						registerOrConst(c)));
				break;

			case OpCode.LE:
				tail.branch(new Le(
						pcToLabel.get(pc + 1),
						pcToLabel.get(pc + 2),
						a == 0,
						registerOrConst(b),
						registerOrConst(c)));
				break;

			case OpCode.CALL:
				tail.append(new Call(a, b, c)).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.FORPREP:
				tail.append(new ForPrep(a, registerOrConst(b))).jumpTo(pcToLabel.get(pc + sbx + 1));
				break;

			case OpCode.TAILCALL:
				tail.append(new AccountingNode.End()).term(new TailCall(a, b, c));
				break;

			case OpCode.RETURN:
				tail.append(new AccountingNode.End()).term(new Return(a, b));
				break;

			case OpCode.FORLOOP: {
				Target cont = pcToLabel.get(pc + sbx + 1);
				Target exit = pcToLabel.get(pc + 1);
				tail.branch(new ForLoop(cont, exit, a, sbx));
			}
				break;

			case OpCode.CLOSURE:
				tail.append(new Closure(prototype, a, b)).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.GETUPVAL:
				tail.append(new GetUpVal(a, b)).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.GETTABUP:
				tail.append(new GetTabUp(a, b)).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.GETTABLE:
				tail.append(new GetTable(a, b, c)).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.SETTABUP:
				tail.append(new SetTabUp(a, b, c)).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.SETUPVAL:
				tail.append(new SetUpVal(a, b)).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.SETTABLE:
				tail.append(new SetTable(a, b, c)).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.NEWTABLE:
				tail.append(new NewTable(a, b, c)).jumpTo(pcToLabel.get(pc + 1));
				break;

			case OpCode.SELF:
			case OpCode.TFORCALL:
			case OpCode.SETLIST:
			case OpCode.VARARG:
				// TODO!

//			default: ownNode = null;
			default: throw new UnsupportedOperationException("Unsupported opcode: " + opcode);
		}

		Check.isNull(tail.get());

	}

}

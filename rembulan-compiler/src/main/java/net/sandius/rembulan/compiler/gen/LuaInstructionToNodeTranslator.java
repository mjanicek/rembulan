package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.AccountingNode;
import net.sandius.rembulan.compiler.gen.block.Branch;
import net.sandius.rembulan.compiler.gen.block.CloseUpvalues;
import net.sandius.rembulan.compiler.gen.block.Exit;
import net.sandius.rembulan.compiler.gen.block.LineInfo;
import net.sandius.rembulan.compiler.gen.block.Linear;
import net.sandius.rembulan.compiler.gen.block.NodeAppender;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.lbc.OpCode;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;
import net.sandius.rembulan.util.ReadOnlyArray;

import static net.sandius.rembulan.compiler.gen.block.LuaInstruction.*;

public class LuaInstructionToNodeTranslator {

	private final Prototype prototype;
	private final ReadOnlyArray<Target> pcToLabel;
	
	public LuaInstructionToNodeTranslator(Prototype prototype, ReadOnlyArray<Target> pcToLabel) {
		Check.notNull(prototype);
		Check.notNull(pcToLabel);

		this.prototype = prototype;
		this.pcToLabel = pcToLabel;
	}
	
	private static int registerOrConst(int i) {
		return OpCode.isK(i) ? -1 - OpCode.indexK(i) : i;
	}

	private class MyNodeAppender {
		private final int pc;
		private final NodeAppender appender;

		public MyNodeAppender(int pc) {
			this.appender = new NodeAppender(pcToLabel.get(pc));
			this.pc = pc;
		}

		public MyNodeAppender append(Linear lin) {
			appender.append(lin);
			return this;
		}

		public void branch(Branch branch) {
			appender.branch(branch);
		}

		public void term(Exit term) {
			appender.append(new AccountingNode.End()).term(term);
		}

//		public void jumpTo(int dest) {
//			appender.jumpTo(target(pc, dest));
//		}

		public Target target(int offset) {
			if (offset == 0) {
				throw new IllegalArgumentException();
			}

			Target jmpTarget = pcToLabel.get(pc + offset);

			if (offset < 0) {
				// this is a backward jump

				Target tgt = new Target();
				NodeAppender appender = new NodeAppender(tgt);
				appender.append(new AccountingNode.Flush())
						.jumpTo(jmpTarget);

				return tgt;
			}
			else {
				return jmpTarget;
			}
		}

		public void jumpToOffset(int offset) {
			appender.jumpTo(target(offset));
		}

		public void toNext() {
			jumpToOffset(1);
		}

	}

	public void translate(int pc) {
		MyNodeAppender appender = new MyNodeAppender(pc);

		int line = prototype.getLineAtPC(pc);

		if (line > 0) {
			appender.append(new LineInfo(line));
		}

		appender.append(new AccountingNode.TickBefore());

		translateBody(appender, prototype.getCode().get(pc));
	}

	private void translateBody(MyNodeAppender appender, int insn) {
		int opcode = OpCode.opCode(insn);
		int a = OpCode.arg_A(insn);
		int b = OpCode.arg_B(insn);
		int bx = OpCode.arg_Bx(insn);
		int c = OpCode.arg_C(insn);
		int sbx = OpCode.arg_sBx(insn);

		switch (opcode) {
			case OpCode.MOVE:
				appender.append(new Move(a, b)).toNext();
				break;

			case OpCode.LOADK:
				appender.append(new LoadK(prototype, a, bx)).toNext();
				break;

			case OpCode.LOADNIL:
				appender.append(new LoadNil(a, b)).toNext();
				break;

			case OpCode.LOADBOOL:
				appender.append(new LoadBool(a, b)).jumpToOffset(c != 0 ? 2 : 1);
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
				appender.append(new BinOp(
						prototype,
						BinOpType.fromOpcode(opcode),
						a,
						registerOrConst(b),
						registerOrConst(c))).toNext();
				break;

			case OpCode.UNM:
			case OpCode.BNOT:
			case OpCode.NOT:
			case OpCode.LEN:
				appender.append(new UnOp(
						prototype,
						UnOpType.fromOpcode(opcode),
						a,
						b)).toNext();
				break;

			case OpCode.CONCAT:
				appender.append(new Concat(a, b, c)).toNext();
				break;


			case OpCode.JMP:
				if (a > 0) {
					appender.append(new CloseUpvalues(a - 1));
				}
				appender.jumpToOffset(sbx + 1);
				break;

			case OpCode.EQ:
				appender.branch(new Eq(
						appender.target(1),
						appender.target(2),
						a == 0,
						registerOrConst(b),
						registerOrConst(c)));
				break;

			case OpCode.LT:
				appender.branch(new Lt(
						appender.target(1),
						appender.target(2),
						a == 0,
						registerOrConst(b),
						registerOrConst(c)));
				break;

			case OpCode.LE:
				appender.branch(new Le(
						appender.target(1),
						appender.target(2),
						a == 0,
						registerOrConst(b),
						registerOrConst(c)));
				break;

			case OpCode.CALL:
				appender.append(new Call(a, b, c)).toNext();
				break;

			case OpCode.FORPREP:
				appender.append(new ForPrep(a, registerOrConst(b))).jumpToOffset(sbx + 1);
				break;

			case OpCode.TAILCALL:
				appender.term(new TailCall(a, b, c));
				break;

			case OpCode.RETURN:
				appender.term(new Return(a, b));
				break;

			case OpCode.FORLOOP: {
				Target cont = appender.target(sbx + 1);
				Target exit = appender.target(1);
				appender.branch(new ForLoop(cont, exit, a, sbx));
			}
				break;

			case OpCode.CLOSURE:
				appender.append(new Closure(prototype, a, b)).toNext();
				break;

			case OpCode.GETUPVAL:
				appender.append(new GetUpVal(a, b)).toNext();
				break;

			case OpCode.GETTABUP:
				appender.append(new GetTabUp(a, b)).toNext();
				break;

			case OpCode.GETTABLE:
				appender.append(new GetTable(a, b, c)).toNext();
				break;

			case OpCode.SETTABUP:
				appender.append(new SetTabUp(a, b, c)).toNext();
				break;

			case OpCode.SETUPVAL:
				appender.append(new SetUpVal(a, b)).toNext();
				break;

			case OpCode.SETTABLE:
				appender.append(new SetTable(a, b, c)).toNext();
				break;

			case OpCode.NEWTABLE:
				appender.append(new NewTable(a, b, c)).toNext();
				break;

			case OpCode.SELF:
			case OpCode.TFORCALL:
			case OpCode.SETLIST:
			case OpCode.VARARG:
				// TODO!

//			default: ownNode = null;
			default: throw new UnsupportedOperationException("Unsupported opcode: " + opcode);
		}

	}

}

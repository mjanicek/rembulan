package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.lbc.OpCode;
import net.sandius.rembulan.util.ReadOnlyArray;

import java.util.Objects;

public class NInsn {

	public static NNode translate(int insn, int pc, int line, ReadOnlyArray<NLabel> pcToLabel) {
		int opcode = OpCode.opCode(insn);
		int a = OpCode.arg_A(insn);
		int b = OpCode.arg_B(insn);
		int c = OpCode.arg_C(insn);
		int sbx = OpCode.arg_sBx(insn);

		NNode ownNode;
		switch (opcode) {
			case OpCode.MOVE:
				ownNode = new Move(a, b).followedBy(pcToLabel.get(pc + 1));
				break;

			case OpCode.LOADK:
				ownNode = new LoadK(a, b).followedBy(pcToLabel.get(pc + 1));
				break;


			case OpCode.LOADNIL:
				ownNode = new LoadNil(a, b).followedBy(pcToLabel.get(pc + 1));
				break;

			case OpCode.LOADBOOL:
				ownNode = new LoadBool(a, b).followedBy(pcToLabel.get(pc + (c != 0 ? 2 : 1)));
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
			case OpCode.SHR: {
				BinOpType op = null;
				switch (opcode) {
					case OpCode.ADD:  op = BinOpType.ADD; break;
					case OpCode.SUB:  op = BinOpType.SUB; break;
					case OpCode.MUL:  op = BinOpType.MUL; break;
					case OpCode.MOD:  op = BinOpType.MOD; break;
					case OpCode.POW:  op = BinOpType.POW; break;
					case OpCode.DIV:  op = BinOpType.DIV; break;
					case OpCode.IDIV: op = BinOpType.IDIV; break;
					case OpCode.BAND: op = BinOpType.BAND; break;
					case OpCode.BOR:  op = BinOpType.BOR; break;
					case OpCode.BXOR: op = BinOpType.BXOR; break;
					case OpCode.SHL:  op = BinOpType.SHL; break;
					case OpCode.SHR:  op = BinOpType.SHR; break;
				}
				ownNode = new BinOp(op, a, b, c).followedBy(pcToLabel.get(pc + 1));

			}
				break;

			case OpCode.UNM:
			case OpCode.BNOT:
			case OpCode.NOT:
			case OpCode.LEN: {
				UnOpType op = null;
				switch (opcode) {
					case OpCode.UNM:  op = UnOpType.UNM; break;
					case OpCode.BNOT: op = UnOpType.BNOT; break;
					case OpCode.NOT:  op = UnOpType.NOT; break;
					case OpCode.LEN:  op = UnOpType.LEN; break;
				}
				ownNode = new UnOp(op, a, b).followedBy(pcToLabel.get(pc + 1));
			}
				break;

			case OpCode.CONCAT:
				ownNode = new Concat(a, b, c).followedBy(pcToLabel.get(pc + 1));
				break;


			case OpCode.JMP: {
				int dest = pc + sbx + 1;

				if (a > 0) ownNode = new NCloseUpvalues(a - 1).followedBy(pcToLabel.get(dest));
				else ownNode = pcToLabel.get(dest);
			}
				break;

			case OpCode.EQ: {
				NLabel left = pcToLabel.get(pc + 1);
				NLabel right = pcToLabel.get(pc + 2);
				Eq eq = new Eq(a, b, c);

				// TODO: NEQ has the branches swapped?
				ownNode = eq.withTrueBranch(left).withFalseBranch(right);
			}
				break;

			case OpCode.CALL:
				ownNode = new Call(a, b, c).followedBy(pcToLabel.get(pc + 1));
				break;

			case OpCode.FORPREP:
				ownNode = new ForPrep(a, b).followedBy(pcToLabel.get(pc + sbx + 1));
				break;

			case OpCode.RETURN:
				ownNode = new NAccountEnd().followedBy(new Return(a, b));
				break;

			case OpCode.FORLOOP: {
				ForLoop fl = new ForLoop(a, sbx);
				NLabel cont = pcToLabel.get(pc + sbx + 1);
				NLabel exit = pcToLabel.get(pc + 1);
				ownNode = fl.withTrueBranch(cont).withFalseBranch(exit);
			}
				break;

			case OpCode.CLOSURE:
				ownNode = new NClosure(a, b).followedBy(pcToLabel.get(pc + 1));
				break;

			case OpCode.GETUPVAL:
				ownNode = new GetUpVal(a, b).followedBy(pcToLabel.get(pc + 1));
				break;

			case OpCode.GETTABUP:
				ownNode = new GetTabUp(a, b).followedBy(pcToLabel.get(pc + 1));
				break;

			case OpCode.GETTABLE:
				ownNode = new GetTable(a, b, c).followedBy(pcToLabel.get(pc + 1));
				break;

			case OpCode.SETTABUP:
				ownNode = new SetTabUp(a, b, c).followedBy(pcToLabel.get(pc + 1));
				break;

			case OpCode.SETUPVAL:
				ownNode = new SetUpVal(a, b).followedBy(pcToLabel.get(pc + 1));
				break;

			case OpCode.SETTABLE:
				ownNode = new SetTable(a, b, c).followedBy(pcToLabel.get(pc + 1));
				break;

			case OpCode.NEWTABLE:
				ownNode = new NewTable(a, b, c).followedBy(pcToLabel.get(pc + 1));
				break;

			case OpCode.SELF:
			case OpCode.TFORCALL:
			case OpCode.SETLIST:
			case OpCode.VARARG:
				// TODO!

//			default: ownNode = null;
			default: throw new UnsupportedOperationException("Unsupported opcode: " + opcode);
		}

		// CPU accounting
		ownNode = new NAccountOne().followedBy(ownNode);

		// line info
		if (line > 0) {
			ownNode = new NLine(line).followedBy(ownNode);
		}

		return ownNode;
	}

	public static class Move extends NUnconditional {
		public final int a;
		public final int b;

		public Move(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public String selfToString() {
			return "MOVE(" + a + "," + b + ")";
		}

	}

	public static class LoadK extends NUnconditional {
		public final int a;
		public final int b;

		public LoadK(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public String selfToString() {
			return "LOADK(" + a + "," + b + ")";
		}

	}

	public static class LoadNil extends NUnconditional {
		public final int a;
		public final int b;

		public LoadNil(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public String selfToString() {
			return "LOADNIL(" + a + "," + b + ")";
		}

	}

	public static class LoadBool extends NUnconditional {
		public final int a;
		public final int b;

		public LoadBool(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public String selfToString() {
			return "LOADBOOL(" + a + "," + b + ")";
		}

	}

	public enum BinOpType {
		ADD, SUB, MUL, MOD, POW, DIV, IDIV, BAND, BOR, BXOR, SHL, SHR
	}

	public enum UnOpType {
		UNM, BNOT, NOT, LEN
	}

	public static class BinOp extends NUnconditional {

		public final BinOpType op;
		public final int a;
		public final int b;
		public final int c;

		public BinOp(BinOpType op, int a, int b, int c) {
			super();
			this.op = Objects.requireNonNull(op);
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String selfToString() {
			return op.toString() + "(" + a + "," + b + "," + c + ")";
		}

	}

	public static class UnOp extends NUnconditional {

		public final UnOpType op;
		public final int a;
		public final int b;

		public UnOp(UnOpType op, int a, int b) {
			super();
			this.op = Objects.requireNonNull(op);
			this.a = a;
			this.b = b;
		}

		@Override
		public String selfToString() {
			return op.toString() + "(" + a + "," + b + ")";
		}

	}

	public static class Concat extends NUnconditional {

		public final int a;
		public final int b;
		public final int c;

		public Concat(int a, int b, int c) {
			super();
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String selfToString() {
			return "CONCAT(" + a + "," + b + "," + c + ")";
		}

	}

	public static class Call extends NUnconditional {

		public final int a;
		public final int b;
		public final int c;

		public Call(int a, int b, int c) {
			super();
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String selfToString() {
			return "CALL(" + a + "," + b + "," + c + ")";
		}

	}

	public static class ForPrep extends NUnconditional {
		public final int a;
		public final int b;

		public ForPrep(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public String selfToString() {
			return "FORPREP(" + a + "," + b + ")";
		}

	}


	private static class GetUpVal extends NUnconditional {
		public final int a;
		public final int b;

		public GetUpVal(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public String selfToString() {
			return "GETUPVAL(" + a + "," + b + ")";
		}

	}

	private static class GetTabUp extends NUnconditional {
		public final int a;
		public final int b;

		public GetTabUp(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public String selfToString() {
			return "GETTABUP(" + a + "," + b + ")";
		}

	}

	public static class GetTable extends NUnconditional {

		public final int a;
		public final int b;
		public final int c;

		public GetTable(int a, int b, int c) {
			super();
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String selfToString() {
			return "GETTABLE(" + a + "," + b + "," + c + ")";
		}

	}

	public static class SetTabUp extends NUnconditional {

		public final int a;
		public final int b;
		public final int c;

		public SetTabUp(int a, int b, int c) {
			super();
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String selfToString() {
			return "SETTABUP(" + a + "," + b + "," + c + ")";
		}

	}

	private static class SetUpVal extends NUnconditional {
		public final int a;
		public final int b;

		public SetUpVal(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public String selfToString() {
			return "SETUPVAL(" + a + "," + b + ")";
		}

	}

	private static class SetTable extends NUnconditional {
		public final int a;
		public final int b;
		public final int c;

		public SetTable(int a, int b, int c) {
			super();
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String selfToString() {
			return "SETTABLE(" + a + "," + b + "," + c + ")";
		}

	}

	private static class NewTable extends NUnconditional {
		public final int a;
		public final int b;
		public final int c;

		public NewTable(int a, int b, int c) {
			super();
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String selfToString() {
			return "NEWTABLE(" + a + "," + b + "," + c + ")";
		}

	}

	private static class Eq extends NBranch {
		public final int a;
		public final int b;
		public final int c;

		public Eq(int a, int b, int c) {
			super();
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String selfToString() {
			return "EQ(" + a + "," + b + "," + c + ")";
		}

	}

	private static class ForLoop extends NBranch {
		public final int a;
		public final int sbx;

		public ForLoop(int a, int sbx) {
			super();
			this.a = a;
			this.sbx = sbx;
		}

		@Override
		public String selfToString() {
			return "FORLOOP(" + a + "," + sbx + ")";
		}

	}

	private static class Return extends NTerminal {
		public final int a;
		public final int b;

		public Return(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public String selfToString() {
			return "RETURN(" + a + "," + b + ")";
		}

	}

}

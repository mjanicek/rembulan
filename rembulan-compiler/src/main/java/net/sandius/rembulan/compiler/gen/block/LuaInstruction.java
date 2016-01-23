package net.sandius.rembulan.compiler.gen.block;

import net.sandius.rembulan.lbc.OpCode;

import java.util.Objects;

public class LuaInstruction {

	public static class Move extends Linear {
		public final int a;
		public final int b;

		public Move(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "MOVE(" + a + "," + b + ")";
		}

	}

	public static class LoadK extends Linear {
		public final int a;
		public final int b;

		public LoadK(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "LOADK(" + a + "," + b + ")";
		}

	}

	public static class LoadNil extends Linear {
		public final int a;
		public final int b;

		public LoadNil(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "LOADNIL(" + a + "," + b + ")";
		}

	}

	public static class LoadBool extends Linear {
		public final int a;
		public final int b;

		public LoadBool(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "LOADBOOL(" + a + "," + b + ")";
		}

	}

	public enum BinOpType {
		ADD, SUB, MUL, MOD, POW, DIV, IDIV, BAND, BOR, BXOR, SHL, SHR;

		public static BinOpType fromOpcode(int opcode) {
			switch (opcode) {
				case OpCode.ADD:  return BinOpType.ADD;
				case OpCode.SUB:  return BinOpType.SUB;
				case OpCode.MUL:  return BinOpType.MUL;
				case OpCode.MOD:  return BinOpType.MOD;
				case OpCode.POW:  return BinOpType.POW;
				case OpCode.DIV:  return BinOpType.DIV;
				case OpCode.IDIV: return BinOpType.IDIV;
				case OpCode.BAND: return BinOpType.BAND;
				case OpCode.BOR:  return BinOpType.BOR;
				case OpCode.BXOR: return BinOpType.BXOR;
				case OpCode.SHL:  return BinOpType.SHL;
				case OpCode.SHR:  return BinOpType.SHR;
				default: return null;
			}
		}
	}

	public enum UnOpType {
		UNM, BNOT, NOT, LEN;

		public static UnOpType fromOpcode(int opcode) {
			switch (opcode) {
				case OpCode.UNM:  return UnOpType.UNM;
				case OpCode.BNOT: return UnOpType.BNOT;
				case OpCode.NOT:  return UnOpType.NOT;
				case OpCode.LEN:  return UnOpType.LEN;
				default: return null;
			}
		}
	}

	public static class BinOp extends Linear {

		public final BinOpType op;
		public final int a;
		public final int b;
		public final int c;

		public BinOp(BinOpType op, int a, int b, int c) {
			this.op = Objects.requireNonNull(op);
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return op.toString() + "(" + a + "," + b + "," + c + ")";
		}

	}

	public static class UnOp extends Linear {

		public final UnOpType op;
		public final int a;
		public final int b;

		public UnOp(UnOpType op, int a, int b) {
			this.op = Objects.requireNonNull(op);
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return op.toString() + "(" + a + "," + b + ")";
		}

	}

	public static class Concat extends Linear {

		public final int a;
		public final int b;
		public final int c;

		public Concat(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "CONCAT(" + a + "," + b + "," + c + ")";
		}

	}

	public static class Call extends Linear {

		public final int a;
		public final int b;
		public final int c;

		public Call(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "CALL(" + a + "," + b + "," + c + ")";
		}

	}

	public static class ForPrep extends Linear {
		public final int a;
		public final int b;

		public ForPrep(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "FORPREP(" + a + "," + b + ")";
		}

	}


	public static class GetUpVal extends Linear {
		public final int a;
		public final int b;

		public GetUpVal(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "GETUPVAL(" + a + "," + b + ")";
		}

	}

	public static class GetTabUp extends Linear {
		public final int a;
		public final int b;

		public GetTabUp(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "GETTABUP(" + a + "," + b + ")";
		}

	}

	public static class GetTable extends Linear {

		public final int a;
		public final int b;
		public final int c;

		public GetTable(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "GETTABLE(" + a + "," + b + "," + c + ")";
		}

	}

	public static class SetTabUp extends Linear {

		public final int a;
		public final int b;
		public final int c;

		public SetTabUp(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "SETTABUP(" + a + "," + b + "," + c + ")";
		}

	}

	public static class SetUpVal extends Linear {
		public final int a;
		public final int b;

		public SetUpVal(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "SETUPVAL(" + a + "," + b + ")";
		}

	}

	public static class SetTable extends Linear {
		public final int a;
		public final int b;
		public final int c;

		public SetTable(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "SETTABLE(" + a + "," + b + "," + c + ")";
		}

	}

	public static class NewTable extends Linear {
		public final int a;
		public final int b;
		public final int c;

		public NewTable(int a, int b, int c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "NEWTABLE(" + a + "," + b + "," + c + ")";
		}

	}

	public static class Eq extends Branch {
		public final int a;
		public final int b;
		public final int c;

		public Eq(Target trueBranch, Target falseBranch, int a, int b, int c) {
			super(trueBranch, falseBranch);
			this.a = a;
			this.b = b;
			this.c = c;
		}

		@Override
		public String toString() {
			return "EQ(" + a + "," + b + "," + c + ")";
		}

	}

	public static class ForLoop extends Branch {
		public final int a;
		public final int sbx;

		public ForLoop(Target trueBranch, Target falseBranch, int a, int sbx) {
			super(trueBranch, falseBranch);
			this.a = a;
			this.sbx = sbx;
		}

		@Override
		public String toString() {
			return "FORLOOP(" + a + "," + sbx + ")";
		}

	}

	public static class Closure extends Linear {

		public final int dest;
		public final int index;

		public Closure(int dest, int index) {
			this.dest = dest;
			this.index = index;
		}

		@Override
		public String toString() {
			return "CLOSURE(" + dest + "," + index + ")";
		}

	}

	public static class Return extends Exit {
		public final int a;
		public final int b;

		public Return(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return "RETURN(" + a + "," + b + ")";
		}

	}

}

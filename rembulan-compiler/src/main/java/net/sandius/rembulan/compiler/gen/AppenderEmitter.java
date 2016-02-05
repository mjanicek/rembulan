package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.BinaryOperation;
import net.sandius.rembulan.compiler.gen.block.CloseUpvalues;
import net.sandius.rembulan.compiler.gen.block.Linear;
import net.sandius.rembulan.compiler.gen.block.LuaInstruction;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.lbc.Prototype;

import java.util.Objects;

import static net.sandius.rembulan.compiler.gen.block.LuaInstruction.registerOrConst;

public class AppenderEmitter implements InstructionEmitter {

	private final LuaInstructionToNodeTranslator.MyNodeAppender appender;

	private final Prototype prototype;

	public AppenderEmitter(Prototype prototype, LuaInstructionToNodeTranslator.MyNodeAppender appender) {
	    this.prototype = prototype;
		this.appender = Objects.requireNonNull(appender);
	}

	protected AppenderEmitter append(Linear lin) {
		appender.append(lin);
		return this;
	}

	@Override
	public void l_MOVE(int a, int b) {
		appender.append(new LuaInstruction.Move(a, b)).toNext();
	}

	@Override
	public void l_LOADK(int a, int bx) {
		appender.append(new LuaInstruction.LoadK(prototype, a, bx)).toNext();
	}

	@Override
	public void l_LOADBOOL(int a, int b, int c) {
		appender.append(new LuaInstruction.LoadBool(a, b)).jumpToOffset(c != 0 ? 2 : 1);
	}

	@Override
	public void l_LOADNIL(int a, int b) {
		appender.append(new LuaInstruction.LoadNil(a, b)).toNext();
	}

	@Override
	public void l_GETUPVAL(int a, int b) {
		appender.append(new LuaInstruction.GetUpVal(a, b)).toNext();
	}

	@Override
	public void l_GETTABUP(int a, int b, int c) {
		appender.append(new LuaInstruction.GetTabUp(a, b)).toNext();
	}

	@Override
	public void l_GETTABLE(int a, int b, int c) {
		appender.append(new LuaInstruction.GetTable(a, b, c)).toNext();
	}

	@Override
	public void l_SETTABUP(int a, int b, int c) {
		appender.append(new LuaInstruction.SetTabUp(a, b, c)).toNext();
	}

	@Override
	public void l_SETUPVAL(int a, int b) {
		appender.append(new LuaInstruction.SetUpVal(a, b)).toNext();
	}

	@Override
	public void l_SETTABLE(int a, int b, int c) {
		appender.append(new LuaInstruction.SetTable(a, b, c)).toNext();
	}

	@Override
	public void l_NEWTABLE(int a, int b, int c) {
		appender.append(new LuaInstruction.NewTable(a, b, c)).toNext();
	}

	@Override
	public void l_SELF(int a, int b, int c) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void l_ADD(int a, int b, int c) {
		appender.append(new BinaryOperation.Add(
				prototype,
				a,
				LuaInstruction.registerOrConst(b),
				LuaInstruction.registerOrConst(c))).toNext();
	}

	@Override
	public void l_SUB(int a, int b, int c) {
		appender.append(new BinaryOperation.Sub(
				prototype,
				a,
				LuaInstruction.registerOrConst(b),
				LuaInstruction.registerOrConst(c))).toNext();
	}

	@Override
	public void l_MUL(int a, int b, int c) {
		appender.append(new BinaryOperation.Mul(
				prototype,
				a,
				LuaInstruction.registerOrConst(b),
				LuaInstruction.registerOrConst(c))).toNext();
	}

	@Override
	public void l_MOD(int a, int b, int c) {
		appender.append(new BinaryOperation.Mod(
				prototype,
				a,
				LuaInstruction.registerOrConst(b),
				LuaInstruction.registerOrConst(c))).toNext();
	}

	@Override
	public void l_POW(int a, int b, int c) {
		appender.append(new BinaryOperation.Pow(
				prototype,
				a,
				LuaInstruction.registerOrConst(b),
				LuaInstruction.registerOrConst(c))).toNext();
	}

	@Override
	public void l_DIV(int a, int b, int c) {
		appender.append(new BinaryOperation.Div(
				prototype,
				a,
				LuaInstruction.registerOrConst(b),
				LuaInstruction.registerOrConst(c))).toNext();
	}

	@Override
	public void l_IDIV(int a, int b, int c) {
		appender.append(new BinaryOperation.IDiv(
				prototype,
				a,
				LuaInstruction.registerOrConst(b),
				LuaInstruction.registerOrConst(c))).toNext();
	}

	@Override
	public void l_BAND(int a, int b, int c) {
		appender.append(new BinaryOperation.BAnd(
				prototype,
				a,
				LuaInstruction.registerOrConst(b),
				LuaInstruction.registerOrConst(c))).toNext();
	}

	@Override
	public void l_BOR(int a, int b, int c) {
		appender.append(new BinaryOperation.BOr(
				prototype,
				a,
				LuaInstruction.registerOrConst(b),
				LuaInstruction.registerOrConst(c))).toNext();
	}

	@Override
	public void l_BXOR(int a, int b, int c) {
		appender.append(new BinaryOperation.BXor(
				prototype,
				a,
				LuaInstruction.registerOrConst(b),
				LuaInstruction.registerOrConst(c))).toNext();
	}

	@Override
	public void l_SHL(int a, int b, int c) {
		appender.append(new BinaryOperation.Shl(
				prototype,
				a,
				LuaInstruction.registerOrConst(b),
				LuaInstruction.registerOrConst(c))).toNext();
	}

	@Override
	public void l_SHR(int a, int b, int c) {
		appender.append(new BinaryOperation.Shr(
				prototype,
				a,
				LuaInstruction.registerOrConst(b),
				LuaInstruction.registerOrConst(c))).toNext();
	}

	@Override
	public void l_UNM(int a, int b) {
		appender.append(new LuaInstruction.UnOp(
				prototype,
				LuaInstruction.UnOpType.UNM,
				a,
				b)).toNext();
	}

	@Override
	public void l_BNOT(int a, int b) {
		appender.append(new LuaInstruction.UnOp(
				prototype,
				LuaInstruction.UnOpType.BNOT,
				a,
				b)).toNext();
	}

	@Override
	public void l_NOT(int a, int b) {
		appender.append(new LuaInstruction.UnOp(
				prototype,
				LuaInstruction.UnOpType.NOT,
				a,
				b)).toNext();
	}

	@Override
	public void l_LEN(int a, int b) {
		appender.append(new LuaInstruction.UnOp(
				prototype,
				LuaInstruction.UnOpType.LEN,
				a,
				b)).toNext();
	}

	@Override
	public void l_CONCAT(int a, int b, int c) {
		appender.append(new LuaInstruction.Concat(a, b, c)).toNext();
	}

	@Override
	public void l_JMP(int a, int sbx) {
		(a > 0 ? appender.append(new CloseUpvalues(a - 1)) : appender)
				.jumpToOffset(sbx + 1);
	}

	@Override
	public void l_EQ(int a, int b, int c) {
		appender.branch(new LuaInstruction.Eq(
				appender.target(1),
				appender.target(2),
				a == 0,
				registerOrConst(b),
				registerOrConst(c)));
	}

	@Override
	public void l_LT(int a, int b, int c) {
		appender.branch(new LuaInstruction.Lt(
				appender.target(1),
				appender.target(2),
				a == 0,
				registerOrConst(b),
				registerOrConst(c)));
	}

	@Override
	public void l_LE(int a, int b, int c) {
		appender.branch(new LuaInstruction.Le(
				appender.target(1),
				appender.target(2),
				a == 0,
				registerOrConst(b),
				registerOrConst(c)));
	}

	@Override
	public void l_TEST(int a, int c) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void l_TESTSET(int a, int b, int c) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void l_CALL(int a, int b, int c) {
		appender.append(new LuaInstruction.Call(a, b, c)).toNext();
	}

	@Override
	public void l_TAILCALL(int a, int b, int c) {
		appender.term(new LuaInstruction.TailCall(a, b, c));
	}

	@Override
	public void l_RETURN(int a, int b) {
		appender.term(new LuaInstruction.Return(a, b));
	}

	@Override
	public void l_FORLOOP(int a, int sbx) {
		Target cont = appender.target(sbx + 1);
		Target exit = appender.target(1);
		appender.branch(new LuaInstruction.ForLoop(cont, exit, a, sbx));
	}

	@Override
	public void l_FORPREP(int a, int sbx) {
		// TODO
		appender.append(new LuaInstruction.ForPrep(a)).jumpToOffset(sbx + 1);
	}

	@Override
	public void l_TFORCALL(int a, int c) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void l_TFORLOOP(int a, int sbx) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void l_SETLIST(int a, int b, int c) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void l_CLOSURE(int a, int bx) {
		appender.append(new LuaInstruction.Closure(prototype, a, bx)).toNext();
	}

	@Override
	public void l_VARARG(int a, int b) {
		appender.append(new LuaInstruction.Vararg(a, b)).toNext();
	}

	@Override
	public void l_EXTRAARG(int ax) {
		throw new UnsupportedOperationException();  // TODO
	}

}

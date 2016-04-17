package net.sandius.rembulan.compiler.gen;

import net.sandius.rembulan.compiler.gen.block.CloseUpvalues;
import net.sandius.rembulan.compiler.gen.block.Linear;
import net.sandius.rembulan.compiler.gen.block.LuaBinaryOperation;
import net.sandius.rembulan.compiler.gen.block.LuaInstruction;
import net.sandius.rembulan.compiler.gen.block.LuaUnaryOperation;
import net.sandius.rembulan.compiler.gen.block.NodeAppender;
import net.sandius.rembulan.compiler.gen.block.Target;
import net.sandius.rembulan.lbc.Prototype;
import net.sandius.rembulan.util.Check;

public class AppenderEmitter implements LuaInstructionVisitor {

	private final LuaInstructionToNodeTranslator.MyNodeAppender appender;

	private final Prototype prototype;

	public AppenderEmitter(Prototype prototype, LuaInstructionToNodeTranslator.MyNodeAppender appender) {
	    this.prototype = prototype;
		this.appender = Check.notNull(appender);
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
		appender.append(new LuaInstruction.LoadK(appender.context(), a, bx)).toNext();
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
		appender.append(new LuaInstruction.GetTabUp(a, b, c)).toNext();
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
		appender.append(new LuaInstruction.Self(a, b, c)).toNext();
	}

	@Override
	public void l_ADD(int a, int b, int c) {
		appender.append(new LuaBinaryOperation(appender.context(), LuaBinaryOperation.Op.ADD, a, b, c)).toNext();
	}

	@Override
	public void l_SUB(int a, int b, int c) {
		appender.append(new LuaBinaryOperation(appender.context(), LuaBinaryOperation.Op.SUB, a, b, c)).toNext();
	}

	@Override
	public void l_MUL(int a, int b, int c) {
		appender.append(new LuaBinaryOperation(appender.context(), LuaBinaryOperation.Op.MUL, a, b, c)).toNext();
	}

	@Override
	public void l_MOD(int a, int b, int c) {
		appender.append(new LuaBinaryOperation(appender.context(), LuaBinaryOperation.Op.MOD, a, b, c)).toNext();
	}

	@Override
	public void l_POW(int a, int b, int c) {
		appender.append(new LuaBinaryOperation(appender.context(), LuaBinaryOperation.Op.POW, a, b, c)).toNext();
	}

	@Override
	public void l_DIV(int a, int b, int c) {
		appender.append(new LuaBinaryOperation(appender.context(), LuaBinaryOperation.Op.DIV, a, b, c)).toNext();
	}

	@Override
	public void l_IDIV(int a, int b, int c) {
		appender.append(new LuaBinaryOperation(appender.context(), LuaBinaryOperation.Op.IDIV, a, b, c)).toNext();
	}

	@Override
	public void l_BAND(int a, int b, int c) {
		appender.append(new LuaBinaryOperation(appender.context(), LuaBinaryOperation.Op.BAND, a, b, c)).toNext();
	}

	@Override
	public void l_BOR(int a, int b, int c) {
		appender.append(new LuaBinaryOperation(appender.context(), LuaBinaryOperation.Op.BOR, a, b, c)).toNext();
	}

	@Override
	public void l_BXOR(int a, int b, int c) {
		appender.append(new LuaBinaryOperation(appender.context(), LuaBinaryOperation.Op.BXOR, a, b, c)).toNext();
	}

	@Override
	public void l_SHL(int a, int b, int c) {
		appender.append(new LuaBinaryOperation(appender.context(), LuaBinaryOperation.Op.SHL, a, b, c)).toNext();
	}

	@Override
	public void l_SHR(int a, int b, int c) {
		appender.append(new LuaBinaryOperation(appender.context(), LuaBinaryOperation.Op.SHR, a, b, c)).toNext();
	}

	@Override
	public void l_UNM(int a, int b) {
		appender.append(new LuaUnaryOperation(LuaUnaryOperation.Op.UNM, a, b)).toNext();
	}

	@Override
	public void l_BNOT(int a, int b) {
		appender.append(new LuaUnaryOperation(LuaUnaryOperation.Op.BNOT, a, b)).toNext();
	}

	@Override
	public void l_NOT(int a, int b) {
		appender.append(new LuaUnaryOperation(LuaUnaryOperation.Op.NOT, a, b)).toNext();
	}

	@Override
	public void l_LEN(int a, int b) {
		appender.append(new LuaUnaryOperation(LuaUnaryOperation.Op.LEN, a, b)).toNext();
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

	/*	A B C	if ((RK(B) == RK(C)) ~= A) then pc++		*/
	@Override
	public void l_EQ(int a, int b, int c) {
		appender.branch(new LuaInstruction.Eq(appender.target(1), appender.target(2), a, b, c));
	}

	/*	A B C	if ((RK(B) <  RK(C)) ~= A) then pc++  		*/
	@Override
	public void l_LT(int a, int b, int c) {
		appender.branch(new LuaInstruction.Lt(appender.target(1), appender.target(2), a, b, c));
	}

	/**	A B C	if ((RK(B) <= RK(C)) ~= A) then pc++  		*/
	@Override
	public void l_LE(int a, int b, int c) {
		appender.branch(new LuaInstruction.Le(appender.target(1), appender.target(2), a, b, c));
	}

	/*	A C	if not (R(A) <=> C) then pc++			*/
	@Override
	public void l_TEST(int a, int c) {
		// TEST is evaluating boolean *in*equality: true branch is when (R(A) <=> C)
		appender.branch(new LuaInstruction.Test(appender.target(1), appender.target(2), a, c));
	}

	// we're translating this a TEST followed by MOVE in the true branch
	/*	A B C	if (R(B) <=> C) then R(A) := R(B) else pc++	*/
	@Override
	public void l_TESTSET(int a, int b, int c) {
		Target trueBranch = new Target();
		NodeAppender app = new NodeAppender(trueBranch);
		app.append(new LuaInstruction.Move(a, b));  // this is the R(A) := R(B)
		app.jumpTo(appender.target(1));

		appender.branch(new LuaInstruction.Test(trueBranch, appender.target(2), b, c));
	}

	@Override
	public void l_CALL(int a, int b, int c) {
		// TODO: check whether this might be needed
//		if (c == 0) {
//			appender.append(new CloseUpvalues(a));
//		}
		appender.append(new LuaInstruction.Call(a, b, c)).toNext();
	}

	@Override
	public void l_TAILCALL(int a, int b, int c) {
		Check.isEq(c, 0);
		appender.term(new LuaInstruction.TailCall(a, b));
	}

	@Override
	public void l_RETURN(int a, int b) {
		appender.term(new LuaInstruction.Return(a, b));
	}

	/*	A sBx	R(A)+=R(A+2); if R(A) <?= R(A+1) then { pc+=sBx; R(A+3)=R(A) }*/
	@Override
	public void l_FORLOOP(int a, int sbx) {
		// splitting this into the loop step and the local variable update in the true branch
		Target trueBranch = new Target();
		NodeAppender app = new NodeAppender(trueBranch);
		app.append(new LuaInstruction.Move(a + 3, a));  // this is the R(A+3) := R(A)
		app.jumpTo(appender.target(sbx + 1));
		appender.branch(new LuaInstruction.ForLoop(trueBranch, appender.target(1), a));
	}

	@Override
	public void l_FORPREP(int a, int sbx) {
		appender.append(new LuaInstruction.ForPrep(a)).jumpToOffset(sbx + 1);
	}

	@Override
	public void l_TFORCALL(int a, int c) {
		appender.append(new LuaInstruction.TForCall(a, c)).toNext();
	}

	/* A sBx   if R(A+1) ~= nil then { R(A)=R(A+1); pc += sBx } */
	@Override
	public void l_TFORLOOP(int a, int sbx) {
		// splitting this into the loop step and local variable update in the true branch
		Target trueBranch = new Target();
		NodeAppender app = new NodeAppender(trueBranch);
		app.append(new LuaInstruction.Move(a, a + 1));  // this is the R(A) := R(A+1)
		app.jumpTo(appender.target(sbx + 1));
		appender.branch(new LuaInstruction.TForLoop(trueBranch, appender.target(1), a));
	}

	@Override
	public void l_SETLIST(int a, int b, int c) {
		throw new UnsupportedOperationException();  // TODO
	}

	@Override
	public void l_CLOSURE(int a, int bx) {
		appender.append(new LuaInstruction.Closure(appender.context(), a, bx)).toNext();
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

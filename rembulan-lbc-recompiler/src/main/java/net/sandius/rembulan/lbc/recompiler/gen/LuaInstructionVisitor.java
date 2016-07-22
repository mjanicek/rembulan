package net.sandius.rembulan.lbc.recompiler.gen;

public interface LuaInstructionVisitor {

	void l_MOVE(int a, int b);
	void l_LOADK(int a, int bx);
	void l_LOADBOOL(int a, int b, int c);
	void l_LOADNIL(int a, int b);
	void l_GETUPVAL(int a, int b);
	void l_GETTABUP(int a, int b, int c);
	void l_GETTABLE(int a, int b, int c);
	void l_SETTABUP(int a, int b, int c);
	void l_SETUPVAL(int a, int b);
	void l_SETTABLE(int a, int b, int c);
	void l_NEWTABLE(int a, int b, int c);
	void l_SELF(int a, int b, int c);

	void l_ADD(int a, int b, int c);
	void l_SUB(int a, int b, int c);
	void l_MUL(int a, int b, int c);
	void l_MOD(int a, int b, int c);
	void l_POW(int a, int b, int c);
	void l_DIV(int a, int b, int c);
	void l_IDIV(int a, int b, int c);
	void l_BAND(int a, int b, int c);
	void l_BOR(int a, int b, int c);
	void l_BXOR(int a, int b, int c);
	void l_SHL(int a, int b, int c);
	void l_SHR(int a, int b, int c);

	void l_UNM(int a, int b);
	void l_BNOT(int a, int b);
	void l_NOT(int a, int b);
	void l_LEN(int a, int b);

	void l_CONCAT(int a, int b, int c);

	void l_JMP(int a, int sbx);
	void l_EQ(int a, int b, int c);
	void l_LT(int a, int b, int c);
	void l_LE(int a, int b, int c);

	void l_TEST(int a, int c);
	void l_TESTSET(int a, int b, int c);

	void l_CALL(int a, int b, int c);
	void l_TAILCALL(int a, int b, int c);
	void l_RETURN(int a, int b);

	void l_FORLOOP(int a, int sbx);
	void l_FORPREP(int a, int sbx);

	void l_TFORCALL(int a, int c);
	void l_TFORLOOP(int a, int sbx);

	void l_SETLIST(int a, int b, int c);

	void l_CLOSURE(int a, int bx);

	void l_VARARG(int a, int b);

	void l_EXTRAARG(int ax);

}

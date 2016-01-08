package net.sandius.rembulan.core;

public class OpCode {

	// from lopcodes.h

	/*
	 ===========================================================================
	 We assume that instructions are unsigned numbers.
	 All instructions have an opcode in the first 6 bits.
	 Instructions can have the following fields:
	 `A' : 8 bits
	 `B' : 9 bits
	 `C' : 9 bits
	 `Bx' : 18 bits (`B' and `C' together)
	 `sBx' : signed Bx

	 A signed argument is represented in excess K that is, the number
	 value is the unsigned value minus K. K is exactly the maximum value
	 for that argument (so that -max is represented by 0, and +max is
	 represented by 2*max), which is half the maximum for the corresponding
	 unsigned argument.
	 ===========================================================================
	*/

	public static final int iABC = 0;
	public static final int iABx = 1;
	public static final int iAsBx = 2;
	public static final int iAx = 3;

	public static final int MOVE     =  0;    /**	A B	R(A) := R(B)					*/
	public static final int LOADK    =  1;    /**	A Bx	R(A) := Kst(Bx)					*/
	public static final int LOADKX   =  2;    /**	A 	R(A) := Kst(extra arg)					*/
	public static final int LOADBOOL =  3;    /**	A B C	R(A) := (Bool)B; if (C) pc++			*/
	public static final int LOADNIL  =  4;    /**	A B	R(A) := ... := R(A+B) := nil			*/
	public static final int GETUPVAL =  5;    /**	A B	R(A) := UpValue[B]				*/

	public static final int GETTABUP =  6;    /**	A B C	R(A) := UpValue[B][RK(C)]			*/
	public static final int GETTABLE =  7;    /**	A B C	R(A) := R(B)[RK(C)]				*/
	public static final int SETTABUP =  8;    /**	A B C	UpValue[A][RK(B)] := RK(C)			*/
	public static final int SETUPVAL =  9;    /**	A B	UpValue[B] := R(A)				*/
	public static final int SETTABLE = 10;    /**	A B C	R(A)[RK(B)] := RK(C)				*/

	public static final int NEWTABLE = 11;    /**	A B C	R(A) := {} (size = B,C)				*/

	public static final int SELF     = 12;    /**	A B C	R(A+1) := R(B); R(A) := R(B)[RK(C)]		*/

	public static final int ADD      = 13;    /**	A B C	R(A) := RK(B) + RK(C)				*/
	public static final int SUB      = 14;    /**	A B C	R(A) := RK(B) - RK(C)				*/
	public static final int MUL      = 15;    /**	A B C	R(A) := RK(B) * RK(C)				*/
	public static final int MOD      = 16;    /**	A B C	R(A) := RK(B) % RK(C)				*/
	public static final int POW      = 17;    /**	A B C	R(A) := RK(B) ^ RK(C)				*/
	public static final int DIV      = 18;    /**	A B C	R(A) := RK(B) / RK(C)				*/
	public static final int IDIV     = 19;    /**	A B C	R(A) := RK(B) // RK(C)				*/
	public static final int BAND     = 20;    /**	A B C	R(A) := RK(B) & RK(C)				*/
	public static final int BOR      = 21;    /**	A B C	R(A) := RK(B) | RK(C)				*/
	public static final int BXOR     = 22;    /**	A B C	R(A) := RK(B) ~ RK(C)				*/
	public static final int SHL      = 23;    /**	A B C	R(A) := RK(B) << RK(C)				*/
	public static final int SHR      = 24;    /**	A B C	R(A) := RK(B) >> RK(C)				*/

	public static final int UNM      = 25;    /**	A B	R(A) := -R(B)					*/
	public static final int BNOT     = 26;    /**	A B	R(A) := ~R(B)				*/
	public static final int NOT      = 27;    /**	A B	R(A) := not R(B)				*/
	public static final int LEN      = 28;    /**	A B	R(A) := length of R(B)				*/

	public static final int CONCAT   = 29;    /**	A B C	R(A) := R(B).. ... ..R(C)			*/

	public static final int JMP      = 30;    /**	sBx	pc+=sBx					*/
	public static final int EQ       = 31;    /**	A B C	if ((RK(B) == RK(C)) ~= A) then pc++		*/
	public static final int LT       = 32;    /**	A B C	if ((RK(B) <  RK(C)) ~= A) then pc++  		*/
	public static final int LE       = 33;    /**	A B C	if ((RK(B) <= RK(C)) ~= A) then pc++  		*/

	public static final int TEST     = 34;    /**	A C	if not (R(A) <=> C) then pc++			*/
	public static final int TESTSET  = 35;    /**	A B C	if (R(B) <=> C) then R(A) := R(B) else pc++	*/

	public static final int CALL     = 36;    /**	A B C	R(A), ... ,R(A+C-2) := R(A)(R(A+1), ... ,R(A+B-1)) */
	public static final int TAILCALL = 37;    /**	A B C	return R(A)(R(A+1), ... ,R(A+B-1))		*/
	public static final int RETURN   = 38;    /**	A B	return R(A), ... ,R(A+B-2)	(see note)	*/

	public static final int FORLOOP  = 39;    /**	A sBx	R(A)+=R(A+2); if R(A) <?= R(A+1) then { pc+=sBx; R(A+3)=R(A) }*/
	public static final int FORPREP  = 40;    /**	A sBx	R(A)-=R(A+2); pc+=sBx				*/

	public static final int TFORCALL = 41;    /** A C	R(A+3), ... ,R(A+2+C) := R(A)(R(A+1), R(A+2));	*/
	public static final int TFORLOOP = 42;    /** A sBx   if R(A+1) ~= nil then { R(A)=R(A+1); pc += sBx } */

	public static final int SETLIST  = 43;    /**	A B C	R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B	*/

	public static final int CLOSURE  = 44;    /**	A Bx	R(A) := closure(KPROTO[Bx], R(A), ... ,R(A+n))	*/

	public static final int VARARG   = 45;    /**	A B	R(A), R(A+1), ..., R(A+B-1) = vararg		*/

	public static final int EXTRAARG = 46;    /** Ax	extra (larger) argument for previous opcode	*/


	public static final int SIZE_C		= 9;
	public static final int SIZE_B		= 9;
	public static final int SIZE_Bx		= (SIZE_C + SIZE_B);
	public static final int SIZE_A		= 8;
	public static final int SIZE_Ax		= (SIZE_C + SIZE_B + SIZE_A);

	public static final int SIZE_OP		= 6;

	public static final int POS_OP		= 0;
	public static final int POS_A		= (POS_OP + SIZE_OP);
	public static final int POS_C		= (POS_A + SIZE_A);
	public static final int POS_B		= (POS_C + SIZE_C);
	public static final int POS_Bx		= POS_C;
	public static final int POS_Ax		= POS_A;

	public static final int MAX_OP          = ((1<<SIZE_OP)-1);
	public static final int MAXARG_A        = ((1<<SIZE_A)-1);
	public static final int MAXARG_B        = ((1<<SIZE_B)-1);
	public static final int MAXARG_C        = ((1<<SIZE_C)-1);
	public static final int MAXARG_Bx       = ((1<<SIZE_Bx)-1);
	public static final int MAXARG_sBx      = (MAXARG_Bx>>1);     	/* `sBx' is signed */
	public static final int MAXARG_Ax       = ((1<<SIZE_Ax)-1);

	public static int fromABC(int opcode, int a, int b, int c) {
		return ((opcode & MAX_OP) << POS_OP)
				+ ((a & MAXARG_A) << POS_A)
				+ ((b & MAXARG_B) << POS_B)
				+ ((c & MAXARG_C) << POS_C);
	}

	public static int fromABx(int opcode, int a, int bx) {
		return ((opcode & MAX_OP) << POS_OP)
				+ ((a & MAXARG_A) << POS_A)
				+ ((bx & MAXARG_Bx) << POS_Bx);
	}

	public static int fromAsBx(int opcode, int a, int sbx) {
		return ((opcode & MAX_OP) << POS_OP)
				+ ((a & MAXARG_A) << POS_A)
				+ (((sbx + MAXARG_sBx) & MAXARG_Bx) << POS_Bx);
	}

	public static int fromAx(int opcode, int ax) {
		return ((opcode & MAX_OP) << POS_OP)
				+ ((ax & MAXARG_A) << POS_Ax);
	}

	public static int opCode(int insn) {
		return (insn >> POS_OP) & MAX_OP;
	}

	public static int arg_A(int insn) {
		return (insn >> POS_A) & MAXARG_A;
	}

	public static int arg_Ax(int insn) {
		return (insn >> POS_Ax) & MAXARG_Ax;
	}

	public static int arg_B(int insn) {
		return (insn >> POS_B) & MAXARG_B;
	}

	public static int arg_C(int insn) {
		return (insn >> POS_C) & MAXARG_C;
	}

	public static int arg_Bx(int insn) {
		return (insn >> POS_Bx) & MAXARG_Bx;
	}

	public static int arg_sBx(int insn) {
		return ((insn >> POS_Bx) & MAXARG_Bx) - MAXARG_sBx;
	}

	/** this bit 1 means constant (0 means register) */
	public static final int BITRK = 1 << (SIZE_B - 1);

	public static boolean isK(int x) {
		return 0 != (x & BITRK);
	}

	public static int indexK(int r) {
		return r & ~BITRK;
	}

	/*
	 * masks for instruction properties. The format is:
	 * bits 0-1: op mode
	 * bits 2-3: C arg mode
	 * bits 4-5: B arg mode
	 * bit 6: instruction set register A
	 * bit 7: operator is a test
	 */

	public static final int OpArgN = 0;  /* argument is not used */
	public static final int OpArgU = 1;  /* argument is used */
	public static final int OpArgR = 2;  /* argument is a register or a jump offset */
	public static final int OpArgK = 3;  /* argument is a constant or register/constant */

	private static final int[] opModes = new int[] {
			/* T          A          B               C            mode       opcode  */
			(0 << 7) | (1 << 6) | (OpArgR << 4) | (OpArgN << 2) | iABC,    /* OP_MOVE */
			(0 << 7) | (1 << 6) | (OpArgK << 4) | (OpArgN << 2) | iABx,    /* OP_LOADK */
			(0 << 7) | (1 << 6) | (OpArgN << 4) | (OpArgN << 2) | iABx,    /* OP_LOADKX */
			(0 << 7) | (1 << 6) | (OpArgU << 4) | (OpArgU << 2) | iABC,    /* OP_LOADBOOL */
			(0 << 7) | (1 << 6) | (OpArgU << 4) | (OpArgN << 2) | iABC,    /* OP_LOADNIL */
			(0 << 7) | (1 << 6) | (OpArgU << 4) | (OpArgN << 2) | iABC,    /* OP_GETUPVAL */
			(0 << 7) | (1 << 6) | (OpArgU << 4) | (OpArgK << 2) | iABC,    /* OP_GETTABUP */
			(0 << 7) | (1 << 6) | (OpArgR << 4) | (OpArgK << 2) | iABC,    /* OP_GETTABLE */
			(0 << 7) | (0 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_SETTABUP */
			(0 << 7) | (0 << 6) | (OpArgU << 4) | (OpArgN << 2) | iABC,    /* OP_SETUPVAL */
			(0 << 7) | (0 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_SETTABLE */
			(0 << 7) | (1 << 6) | (OpArgU << 4) | (OpArgU << 2) | iABC,    /* OP_NEWTABLE */
			(0 << 7) | (1 << 6) | (OpArgR << 4) | (OpArgK << 2) | iABC,    /* OP_SELF */
			(0 << 7) | (1 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_ADD */
			(0 << 7) | (1 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_SUB */
			(0 << 7) | (1 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_MUL */
			(0 << 7) | (1 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_MOD */
			(0 << 7) | (1 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_POW */
			(0 << 7) | (1 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_DIV */
			(0 << 7) | (1 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_IDIV */
			(0 << 7) | (1 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_BAND */
			(0 << 7) | (1 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_BOR */
			(0 << 7) | (1 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_BXOR */
			(0 << 7) | (1 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_SHL */
			(0 << 7) | (1 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_SHR */
			(0 << 7) | (1 << 6) | (OpArgR << 4) | (OpArgN << 2) | iABC,    /* OP_UNM */
			(0 << 7) | (1 << 6) | (OpArgR << 4) | (OpArgN << 2) | iABC,    /* OP_BNOT */
			(0 << 7) | (1 << 6) | (OpArgR << 4) | (OpArgN << 2) | iABC,    /* OP_NOT */
			(0 << 7) | (1 << 6) | (OpArgR << 4) | (OpArgN << 2) | iABC,    /* OP_LEN */
			(0 << 7) | (1 << 6) | (OpArgR << 4) | (OpArgR << 2) | iABC,    /* OP_CONCAT */
			(0 << 7) | (0 << 6) | (OpArgR << 4) | (OpArgN << 2) | iAsBx,   /* OP_JMP */
			(1 << 7) | (0 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_EQ */
			(1 << 7) | (0 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_LT */
			(1 << 7) | (0 << 6) | (OpArgK << 4) | (OpArgK << 2) | iABC,    /* OP_LE */
			(1 << 7) | (0 << 6) | (OpArgN << 4) | (OpArgU << 2) | iABC,    /* OP_TEST */
			(1 << 7) | (1 << 6) | (OpArgR << 4) | (OpArgU << 2) | iABC,    /* OP_TESTSET */
			(0 << 7) | (1 << 6) | (OpArgU << 4) | (OpArgU << 2) | iABC,    /* OP_CALL */
			(0 << 7) | (1 << 6) | (OpArgU << 4) | (OpArgU << 2) | iABC,    /* OP_TAILCALL */
			(0 << 7) | (0 << 6) | (OpArgU << 4) | (OpArgN << 2) | iABC,    /* OP_RETURN */
			(0 << 7) | (1 << 6) | (OpArgR << 4) | (OpArgN << 2) | iAsBx,   /* OP_FORLOOP */
			(0 << 7) | (1 << 6) | (OpArgR << 4) | (OpArgN << 2) | iAsBx,   /* OP_FORPREP */
			(0 << 7) | (0 << 6) | (OpArgN << 4) | (OpArgU << 2) | iABC,    /* OP_TFORCALL */
			(0 << 7) | (1 << 6) | (OpArgR << 4) | (OpArgN << 2) | iAsBx,   /* OP_TFORLOOP */
			(0 << 7) | (0 << 6) | (OpArgU << 4) | (OpArgU << 2) | iABC,    /* OP_SETLIST */
			(0 << 7) | (1 << 6) | (OpArgU << 4) | (OpArgN << 2) | iABx,    /* OP_CLOSURE */
			(0 << 7) | (1 << 6) | (OpArgU << 4) | (OpArgN << 2) | iABC,    /* OP_VARARG */
			(0 << 7) | (0 << 6) | (OpArgU << 4) | (OpArgU << 2) | iAx      /* OP_EXTRAARG */
	};

	public static int getOpMode(int opcode) {
		return opModes[opcode] & 3;
	}

	public static int getBMode(int opcode) {
		return (opModes[opcode] >> 4) & 3;
	}

	public static int getCMode(int opcode) {
		return (opModes[opcode] >> 2) & 3;
	}

	public static boolean testAMode(int opcode) {
		return 0 != (opModes[opcode] & (1 << 6));
	}

	public static boolean testTMode(int opcode) {
		return 0 != (opModes[opcode] & (1 << 7));
	}

}

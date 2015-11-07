package net.sandius.rembulan.core;

public class OpCode {

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

}

package net.sandius.rembulan.parser.ast;

public interface Operator {

	enum Binary implements Operator {

		ADD,
		SUB,
		MUL,
		DIV,
		MOD,
		POW,
		IDIV,

		CONCAT,

		BAND,
		BOR,
		BXOR,
		SHL,
		SHR,

		EQ,
		NEQ,
		LT,
		LE,
		GT,
		GE,

		AND,
		OR

	}

	enum Unary implements Operator {

		UNM,
		BNOT,
		LEN,
		NOT

	}

}

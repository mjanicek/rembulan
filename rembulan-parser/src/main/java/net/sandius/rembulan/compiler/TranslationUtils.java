package net.sandius.rembulan.compiler;

import net.sandius.rembulan.compiler.ir.*;
import net.sandius.rembulan.parser.analysis.ResolvedVariable;
import net.sandius.rembulan.parser.analysis.Variable;
import net.sandius.rembulan.parser.ast.*;
import net.sandius.rembulan.parser.ast.util.AttributeUtils;

abstract class TranslationUtils {

	private TranslationUtils() {
		// not to be instantiated
	}

	public static BinOp.Op bop(Operator.Binary bop) {
		switch (bop) {
			case ADD:  return BinOp.Op.ADD;
			case SUB:  return BinOp.Op.SUB;
			case MUL:  return BinOp.Op.MUL;
			case DIV:  return BinOp.Op.DIV;
			case IDIV: return BinOp.Op.IDIV;
			case MOD:  return BinOp.Op.MOD;
			case POW:  return BinOp.Op.POW;

			case CONCAT: return BinOp.Op.CONCAT;

			case BAND:  return BinOp.Op.BAND;
			case BOR:   return BinOp.Op.BOR;
			case BXOR:  return BinOp.Op.BXOR;
			case SHL:   return BinOp.Op.SHL;
			case SHR:   return BinOp.Op.SHR;

			case EQ:  return BinOp.Op.EQ;
			case NEQ: return BinOp.Op.NEQ;
			case LT:  return BinOp.Op.LT;
			case LE:  return BinOp.Op.LE;

			default: return null;
		}
	}

	public static UnOp.Op uop(Operator.Unary uop) {
		switch (uop) {
			case UNM:  return UnOp.Op.UNM;
			case BNOT: return UnOp.Op.BNOT;
			case LEN:  return UnOp.Op.LEN;
			case NOT:  return UnOp.Op.NOT;

			default:  return null;
		}
	}

	public static ResolvedVariable resolved(VarExpr e) {
		ResolvedVariable rv = e.attributes().get(ResolvedVariable.class);
		if (rv == null) {
			throw new IllegalStateException("Unresolved variable '" + e.name().value() + "' at " + AttributeUtils.sourceInfoString(e));
		}
		return rv;
	}

	public static UpVar upVar(Variable v) {
		throw new UnsupportedOperationException();  // TODO
	}

	public static Var var(Variable variable) {
		throw new UnsupportedOperationException();  // TODO
	}

}

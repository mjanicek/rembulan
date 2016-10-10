/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.compiler.analysis;

import net.sandius.rembulan.ByteString;
import net.sandius.rembulan.Conversions;
import net.sandius.rembulan.LuaMathOperators;
import net.sandius.rembulan.Ordering;
import net.sandius.rembulan.compiler.IRFunc;
import net.sandius.rembulan.compiler.analysis.types.LiteralType;
import net.sandius.rembulan.compiler.analysis.types.LuaTypes;
import net.sandius.rembulan.compiler.analysis.types.Type;
import net.sandius.rembulan.compiler.ir.BinOp;
import net.sandius.rembulan.compiler.ir.UnOp;
import net.sandius.rembulan.runtime.Dispatch;

public class Typer {

	private static Object literalValue(Type t) {
		return t instanceof LiteralType ? ((LiteralType<?>) t).value() : null;
	}

	private static LiteralType<?> objectToLiteralType(Object o) {
		if (o instanceof Number) {
			Number n = (Number) o;
			if (n instanceof Double || n instanceof Float) {
				return LuaTypes.NUMBER_FLOAT.newLiteralType(n.doubleValue());
			}
			else {
				return LuaTypes.NUMBER_INTEGER.newLiteralType(n.longValue());
			}
		}
		else if (o instanceof String) {
			return LuaTypes.STRING.newLiteralType((String) o);
		}
		else if (o instanceof Boolean) {
			return LuaTypes.BOOLEAN.newLiteralType((Boolean) o);
		}
		else {
			return null;
		}
	}

	private static Object tryEmulateArithmeticOperation(BinOp.Op op, Object l, Object r) {
		Number nl = Conversions.arithmeticValueOf(l);
		Number nr = Conversions.arithmeticValueOf(r);

		if (nl == null || nr == null) {
			return null;
		}

		try {
			switch (op) {
				case ADD:  return Dispatch.add(nl, nr);
				case SUB:  return Dispatch.sub(nl, nr);
				case MUL:  return Dispatch.mul(nl, nr);
				case DIV:  return Dispatch.div(nl, nr);
				case MOD:  return Dispatch.mod(nl, nr);
				case IDIV: return Dispatch.idiv(nl, nr);
				case POW:  return Dispatch.pow(nl, nr);
				default: throw new IllegalArgumentException("Illegal operation: " + op);
			}
		}
		catch (ArithmeticException ex) {
			return null;
		}
	}

	private static Object tryEmulateBitwiseOperation(BinOp.Op op, Object l, Object r) {
		Long il = Conversions.integerValueOf(l);
		Long ir = Conversions.integerValueOf(r);

		if (il == null || ir == null) {
			return null;
		}

		switch (op) {
			case BAND: return LuaMathOperators.band(il, ir);
			case BOR:  return LuaMathOperators.bor(il, ir);
			case BXOR: return LuaMathOperators.bxor(il, ir);
			case SHL:  return LuaMathOperators.shl(il, ir);
			case SHR:  return LuaMathOperators.shr(il, ir);
			default: throw new IllegalArgumentException("Illegal operation: " + op);
		}
	}

	private static ByteString tryEmulateConcatOperation(Object l, Object r) {
		ByteString sl = Conversions.stringValueOf(l);
		ByteString sr = Conversions.stringValueOf(r);

		if (sl == null || sr == null) {
			return null;
		}

		return sl.concat(sr);
	}

	private static Object tryEmulateComparisonOperation(BinOp.Op op, Object l, Object r) {
		if (l == null || r == null) {
			return null;
		}

		Ordering<Object> c = Ordering.of(l, r);

		if (c == null) {
			return null;
		}

		switch (op) {
			case EQ:  return c.eq(l, r);
			case NEQ: return !c.eq(l, r);
			case LT:  return c.lt(l, r);
			case LE:  return c.le(l, r);
			default: throw new IllegalArgumentException("Illegal operation: " + op);
		}
	}

	private static Object tryEmulateOperation(BinOp.Op op, Object l, Object r) {
		if (l == null || r == null) {
			return null;
		}

		switch (op) {

			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case MOD:
			case IDIV:
			case POW:
				return tryEmulateArithmeticOperation(op, l, r);

			case BAND:
			case BOR:
			case BXOR:
			case SHL:
			case SHR:
				return tryEmulateBitwiseOperation(op, l, r);

			case CONCAT:
				return tryEmulateConcatOperation(l, r);

			case EQ:
			case NEQ:
			case LT:
			case LE:
				return tryEmulateComparisonOperation(op, l, r);

			default:
				throw new IllegalArgumentException("Illegal operation: " + op);
		}
	}

	private static Object tryEmulateOperation(UnOp.Op op, Object arg) {
		if (arg == null) {
			return null;
		}

		switch (op) {
			case UNM: {
				Number n = Conversions.arithmeticValueOf(arg);
				return n != null ? Dispatch.unm(n) : null;
			}

			case BNOT: {
				Long l = Conversions.integerValueOf(arg);
				return l != null ? LuaMathOperators.bnot(l) : null;
			}

			case NOT:
				return arg.equals(Boolean.FALSE);

			case LEN:
				if (arg instanceof String) return Dispatch.len((String) arg);
				else if (arg instanceof ByteString) return Long.valueOf(((ByteString) arg).length());
				else return null;

			default:
				throw new IllegalArgumentException("Illegal operation: " + op);
		}
	}

	static LiteralType<?> emulateOp(BinOp.Op op, Type l, Type r) {
		Object result = tryEmulateOperation(op, literalValue(l), literalValue(r));
		return result != null ? objectToLiteralType(result) : null;
	}

	static LiteralType<?> emulateOp(UnOp.Op op, Type t) {
		Object result = tryEmulateOperation(op, literalValue(t));
		return result != null ? objectToLiteralType(result) : null;
	}


	public static TypeInfo analyseTypes(IRFunc fn) {
		TyperVisitor visitor = new TyperVisitor();
		visitor.visit(fn);
		return visitor.valTypes();
	}

}

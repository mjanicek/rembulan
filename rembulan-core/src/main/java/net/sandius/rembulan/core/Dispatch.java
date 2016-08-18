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

package net.sandius.rembulan.core;

import net.sandius.rembulan.LuaMathOperators;

public abstract class Dispatch {

	private Dispatch() {
		// not to be instantiated or extended
	}

	public static Invokable callTarget(MetatableProvider metatableProvider, Object target) {
		if (target instanceof Invokable) {
			return (Invokable) target;
		}
		else {
			Object handler = Metatables.getMetamethod(metatableProvider, Metatables.MT_CALL, target);

			if (handler instanceof Invokable) {
				return (Invokable) handler;
			}
			else {
				throw IllegalOperationAttemptException.call(target);
			}
		}
	}

	public static void mt_invoke(ExecutionContext context, Object target) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) fn.invoke(context);
		else fn.invoke(context, target);
	}

	public static void mt_invoke(ExecutionContext context, Object target, Object arg1) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) fn.invoke(context, arg1);
		else fn.invoke(context, target, arg1);
	}

	public static void mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) fn.invoke(context, arg1, arg2);
		else fn.invoke(context, target, arg1, arg2);
	}

	public static void mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) fn.invoke(context, arg1, arg2, arg3);
		else fn.invoke(context, target, arg1, arg2, arg3);
	}

	public static void mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) fn.invoke(context, arg1, arg2, arg3, arg4);
		else fn.invoke(context, target, arg1, arg2, arg3, arg4);
	}

	public static void mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) fn.invoke(context, arg1, arg2, arg3, arg4, arg5);
		else fn.invoke(context, new Object[] { target, arg1, arg2, arg3, arg4, arg5 });
	}

	public static void mt_invoke(ExecutionContext context, Object target, Object[] args) throws ControlThrowable {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) {
			fn.invoke(context, args);
		}
		else {
			Object[] mtArgs = new Object[args.length + 1];
			mtArgs[0] = target;
			System.arraycopy(args, 0, mtArgs, 1, args.length);
			fn.invoke(context, mtArgs);
		}
	}

	public static void evaluateTailCalls(ExecutionContext context) throws ControlThrowable {
		ReturnVector r = context.getReturnVector();
		while (r.isTailCall()) {
			Object target = r.getTailCallTarget();
			switch (r.size()) {
				case 0: mt_invoke(context, target); break;
				case 1: mt_invoke(context, target, r._0()); break;
				case 2: mt_invoke(context, target, r._0(), r._1()); break;
				case 3: mt_invoke(context, target, r._0(), r._1(), r._2()); break;
				case 4: mt_invoke(context, target, r._0(), r._1(), r._2(), r._3()); break;
				case 5: mt_invoke(context, target, r._0(), r._1(), r._2(), r._3(), r._4()); break;
				default: mt_invoke(context, target, r.toArray()); break;
			}
		}
	}

	public static void call(ExecutionContext context, Object target) throws ControlThrowable {
		mt_invoke(context, target);
		evaluateTailCalls(context);
	}

	public static void call(ExecutionContext context, Object target, Object arg1) throws ControlThrowable {
		mt_invoke(context, target, arg1);
		evaluateTailCalls(context);
	}

	public static void call(ExecutionContext context, Object target, Object arg1, Object arg2) throws ControlThrowable {
		mt_invoke(context, target, arg1, arg2);
		evaluateTailCalls(context);
	}

	public static void call(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		mt_invoke(context, target, arg1, arg2, arg3);
		evaluateTailCalls(context);
	}

	public static void call(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		mt_invoke(context, target, arg1, arg2, arg3, arg4);
		evaluateTailCalls(context);
	}

	public static void call(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		mt_invoke(context, target, arg1, arg2, arg3, arg4, arg5);
		evaluateTailCalls(context);
	}

	public static void call(ExecutionContext context, Object target, Object[] args) throws ControlThrowable {
		mt_invoke(context, target, args);
		evaluateTailCalls(context);
	}

	private static void try_mt_arithmetic(ExecutionContext context, String event, Object a, Object b) throws ControlThrowable {
		Object handler = Metatables.binaryHandlerFor(context.getState(), event, a, b);

		if (handler != null) {
			call(context, handler, a, b);
		}
		else {
			throw IllegalOperationAttemptException.arithmetic(a, b);
		}
	}

	private static void try_mt_arithmetic(ExecutionContext context, String event, Object o) throws ControlThrowable {
		Object handler = Metatables.getMetamethod(context.getState(), event, o);

		if (handler != null) {
			call(context, handler, o);
		}
		else {
			throw IllegalOperationAttemptException.arithmetic(o);
		}
	}

	public static void add(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic math = Arithmetic.of(na, nb);

		if (math != null) {
			context.getReturnVector().setTo(math.add(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_ADD, a, b);
		}
	}

	public static Number add(Number a, Number b) {
		return Arithmetic.of(a, b).add(a, b);
	}

	public static void sub(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic m = Arithmetic.of(na, nb);
		if (m != null) {
			context.getReturnVector().setTo(m.sub(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_SUB, a, b);
		}
	}

	public static Number sub(Number a, Number b) {
		return Arithmetic.of(a, b).sub(a, b);
	}

	public static void mul(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic m = Arithmetic.of(na, nb);
		if (m != null) {
			context.getReturnVector().setTo(m.mul(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_MUL, a, b);
		}
	}

	public static Number mul(Number a, Number b) {
		return Arithmetic.of(a, b).mul(a, b);
	}

	public static void div(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic m = Arithmetic.of(na, nb);
		if (m != null) {
			context.getReturnVector().setTo(m.div(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_DIV, a, b);
		}
	}

	public static Number div(Number a, Number b) {
		return Arithmetic.of(a, b).div(a, b);
	}

	public static void mod(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic m = Arithmetic.of(na, nb);
		if (m != null) {
			context.getReturnVector().setTo(m.mod(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_MOD, a, b);
		}
	}

	public static Number mod(Number a, Number b) {
		return Arithmetic.of(a, b).mod(a, b);
	}

	public static void idiv(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic m = Arithmetic.of(na, nb);
		if (m != null) {
			context.getReturnVector().setTo(m.idiv(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_IDIV, a, b);
		}
	}

	public static Number idiv(Number a, Number b) {
		return Arithmetic.of(a, b).idiv(a, b);
	}

	public static void pow(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Number na = Conversions.arithmeticValueOf(a);
		Number nb = Conversions.arithmeticValueOf(b);
		Arithmetic m = Arithmetic.of(na, nb);
		if (m != null) {
			context.getReturnVector().setTo(m.pow(na, nb));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_POW, a, b);
		}
	}

	public static Number pow(Number a, Number b) {
		return Arithmetic.of(a, b).pow(a, b);
	}

	private static void try_mt_bitwise(ExecutionContext context, String event, Object a, Object b) throws ControlThrowable {
		Object handler = Metatables.binaryHandlerFor(context.getState(), event, a, b);

		if (handler != null) {
			call(context, handler, a, b);
		}
		else {
			throw IllegalOperationAttemptException.bitwise(a, b);
		}
	}

	private static void try_mt_bitwise(ExecutionContext context, String event, Object o) throws ControlThrowable {
		Object handler = Metatables.getMetamethod(context.getState(), event, o);

		if (handler != null) {
			call(context, handler, o);
		}
		else {
			throw IllegalOperationAttemptException.bitwise(o);
		}
	}

	public static void band(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.integerValueOf(a);
		Long lb = Conversions.integerValueOf(b);

		if (la != null && lb != null) {
			context.getReturnVector().setTo(LuaMathOperators.band(la, lb));
		}
		else {
			try_mt_bitwise(context, Metatables.MT_BAND, a, b);
		}
	}

	public static void bor(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.integerValueOf(a);
		Long lb = Conversions.integerValueOf(b);

		if (la != null && lb != null) {
			context.getReturnVector().setTo(LuaMathOperators.bor(la, lb));
		}
		else {
			try_mt_bitwise(context, Metatables.MT_BOR, a, b);
		}
	}

	public static void bxor(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.integerValueOf(a);
		Long lb = Conversions.integerValueOf(b);

		if (la != null && lb != null) {
			context.getReturnVector().setTo(LuaMathOperators.bxor(la, lb));
		}
		else {
			try_mt_bitwise(context, Metatables.MT_BXOR, a, b);
		}
	}

	public static void shl(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.integerValueOf(a);
		Long lb = Conversions.integerValueOf(b);

		if (la != null && lb != null) {
			context.getReturnVector().setTo(LuaMathOperators.shl(la, lb));
		}
		else {
			try_mt_bitwise(context, Metatables.MT_SHL, a, b);
		}
	}

	public static void shr(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.integerValueOf(a);
		Long lb = Conversions.integerValueOf(b);

		if (la != null && lb != null) {
			context.getReturnVector().setTo(LuaMathOperators.shr(la, lb));
		}
		else {
			try_mt_bitwise(context, Metatables.MT_SHR, a, b);
		}
	}

	public static void unm(ExecutionContext context, Object o) throws ControlThrowable {
		Number no = Conversions.arithmeticValueOf(o);
		Arithmetic m = Arithmetic.of(no);
		if (m != null) {
			context.getReturnVector().setTo(m.unm(no));
		}
		else {
			try_mt_arithmetic(context, Metatables.MT_UNM, o);
		}
	}

	public static Number unm(Number n) {
		return Arithmetic.of(n).unm(n);
	}

	public static void bnot(ExecutionContext context, Object o) throws ControlThrowable {
		Long lo = Conversions.integerValueOf(o);

		if (lo != null) {
			context.getReturnVector().setTo(LuaMathOperators.bnot(lo));
		}
		else {
			try_mt_bitwise(context, Metatables.MT_BNOT, o);
		}
	}

	public static long len(String s) {
		return s.getBytes().length;  // FIXME: wasteful!
	}

	public static void len(ExecutionContext context, Object o) throws ControlThrowable {
		if (o instanceof String) {
			context.getReturnVector().setTo(len((String) o));
		}
		else {
			Object handler = Metatables.getMetamethod(context.getState(), Metatables.MT_LEN, o);
			if (handler != null) {
				call(context, handler, o);
			}
			else if (o instanceof Table) {
				context.getReturnVector().setTo((long) ((Table) o).rawlen());
			}
			else {
				throw IllegalOperationAttemptException.length(o);
			}
		}
	}

	public static void concat(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		String sa = Conversions.stringValueOf(a);
		String sb = Conversions.stringValueOf(b);

		if (sa != null && sb != null) {
			context.getReturnVector().setTo(sa.concat(sb));
		}
		else {
			Object handler = Metatables.binaryHandlerFor(context.getState(), Metatables.MT_CONCAT, a, b);
			if (handler != null) {
				call(context, handler, a, b);
			}
			else {
				throw IllegalOperationAttemptException.concatenate(a, b);
			}
		}
	}

	private static class ComparisonResumable implements Resumable {

		@Override
		public void resume(ExecutionContext context, Object suspendedState) throws ControlThrowable {
			Boolean b = (Boolean) suspendedState;
			ReturnVector result = context.getReturnVector();
			boolean resultValue = Conversions.booleanValueOf(result._0());
			result.setTo(b == resultValue);
		}

	}

	private static void _call_comparison_mt(ExecutionContext context, boolean cmpTo, Object handler, Object a, Object b) throws ControlThrowable {
		try {
			call(context, handler, a, b);
		}
		catch (ControlThrowable ct) {
			// suspended in the metamethod call
			ct.push(new ComparisonResumable(), cmpTo);
			throw ct;
		}
		// not suspended: set the result, possibly flipping it
		ReturnVector result = context.getReturnVector();
		result.setTo(Conversions.booleanValueOf(result._0()) == cmpTo);
	}

	private static void eq(ExecutionContext context, boolean polarity, Object a, Object b) throws ControlThrowable {
		boolean rawEqual = Ordering.isRawEqual(a, b);

		if (!rawEqual
				&& ((a instanceof Table && b instanceof Table)
				|| (a instanceof Userdata && b instanceof Userdata))) {

			Object handler = Metatables.binaryHandlerFor(context.getState(), Metatables.MT_EQ, a, b);

			if (handler != null) {
				_call_comparison_mt(context, polarity, handler, a, b);
				return;
			}

			// else keep the result as false
		}

		context.getReturnVector().setTo(rawEqual == polarity);
	}

	public static void eq(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		eq(context, true, a, b);
	}

	public static void neq(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		eq(context, false, a, b);
	}

	public static boolean eq(Number a, Number b) {
		return Ordering.NUMERIC.eq(a, b);
	}


	public static void lt(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Ordering c = Ordering.of(a, b);
		if (c != null) {
			@SuppressWarnings("unchecked")
			boolean result = c.lt(a, b);
			context.getReturnVector().setTo(result);
		}
		else {
			Object handler = Metatables.binaryHandlerFor(context.getState(), Metatables.MT_LT, a, b);

			if (handler != null) {
				_call_comparison_mt(context, true, handler, a, b);
			}
			else {
				throw IllegalOperationAttemptException.comparison(a, b);
			}
		}
	}

	public static boolean lt(Number a, Number b) {
		return Ordering.NUMERIC.lt(a, b);
	}

	public static boolean lt(String a, String b) {
		return Ordering.STRING.lt(a, b);
	}

	public static void le(ExecutionContext context, Object a, Object b) throws ControlThrowable {
		Ordering c = Ordering.of(a, b);
		if (c != null) {
			@SuppressWarnings("unchecked")
			boolean result = c.le(a, b);
			context.getReturnVector().setTo(result);
		}
		else {
			LuaState state = context.getState();
			Object le_handler = Metatables.binaryHandlerFor(state, Metatables.MT_LE, a, b);

			if (le_handler != null) {
				_call_comparison_mt(context, true, le_handler, a, b);
			}
			else {
				// TODO: verify that (a, b) is the order in which the metamethod is looked up
				Object lt_handler = Metatables.binaryHandlerFor(state, Metatables.MT_LT, a, b);

				if (lt_handler != null) {
					// will be evaluating "not (b < a)"
					_call_comparison_mt(context, false, lt_handler, b, a);
				}
				else {
					throw IllegalOperationAttemptException.comparison(a, b);
				}
			}
		}
	}

	public static boolean le(Number a, Number b) {
		return Ordering.NUMERIC.le(a, b);
	}

	public static boolean le(String a, String b) {
		return Ordering.STRING.le(a, b);
	}

	public static void index(ExecutionContext context, Object table, Object key) throws ControlThrowable {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object value = t.rawget(key);

			if (value != null) {
				context.getReturnVector().setTo(value);
				return;
			}
			// else fall through and check the __index a metamethod
		}

		Object handler = Metatables.getMetamethod(context.getState(), Metatables.MT_INDEX, table);

		if (handler == null && table instanceof Table) {
			// key not found and no index metamethod, returning nil
			context.getReturnVector().setTo(null);
			return;
		}
		if (handler instanceof Invokable) {
			// call the handler
			Invokable fn = (Invokable) handler;

			fn.invoke(context, table, key);
			evaluateTailCalls(context);
		}
		else if (handler instanceof Table) {
			// TODO: protect against infinite loops
			index(context, handler, key);
		}
		else {
			throw IllegalOperationAttemptException.index(table, key);
		}
	}

	public static void newindex(ExecutionContext context, Object table, Object key, Object value) throws ControlThrowable {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object r = t.rawget(key);

			if (r != null) {
				t.rawset(key, value);
				return;
			}
		}

		Object handler = Metatables.getMetamethod(context.getState(), Metatables.MT_NEWINDEX, table);

		if (handler == null && table instanceof Table) {
			Table t = (Table) table;
			t.rawset(key, value);
			return;
		}

		if (handler instanceof Invokable) {
			// call the handler
			Invokable fn = (Invokable) handler;

			fn.invoke(context, table, key, value);
			evaluateTailCalls(context);
		}
		else if (handler instanceof Table) {
			// TODO: protect against infinite loops
			newindex(context, handler, key, value);
		}
		else {
			throw IllegalOperationAttemptException.index(table, key);
		}
	}

	private static final Long ZERO = Long.valueOf(0L);

	public static boolean continueLoop(Number index, Number limit, Number step) {
		if (Ordering.NUMERIC.eq(ZERO, step)) {
			return false;  // step is zero or NaN
		}
		return Ordering.NUMERIC.lt(ZERO, step)  // ascending?
				? Ordering.NUMERIC.le(index, limit)
				: Ordering.NUMERIC.le(limit, index);
	}

}

package net.sandius.rembulan.core;

import java.io.Serializable;

public abstract class Dispatch {

	private Dispatch() {
		// not to be instantiated or extended
	}

	public static Invokable callTarget(LuaState state, Object target) {
		if (target instanceof Invokable) {
			return (Invokable) target;
		}
		else {
			Object handler = Metatables.getMetamethod(state, Metatables.MT_CALL, target);

			if (handler instanceof Invokable) {
				return (Invokable) handler;
			}
			else {
				throw IllegalOperationAttemptException.call(target);
			}
		}
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target) throws ControlThrowable {
		Invokable fn = callTarget(state, target);
		if (fn == target) fn.invoke(state, result);
		else fn.invoke(state, result, target);
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target, Object arg1) throws ControlThrowable {
		Invokable fn = callTarget(state, target);
		if (fn == target) fn.invoke(state, result, arg1);
		else fn.invoke(state, result, target, arg1);
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2) throws ControlThrowable {
		Invokable fn = callTarget(state, target);
		if (fn == target) fn.invoke(state, result, arg1, arg2);
		else fn.invoke(state, result, target, arg1, arg2);
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		Invokable fn = callTarget(state, target);
		if (fn == target) fn.invoke(state, result, arg1, arg2, arg3);
		else fn.invoke(state, result, target, arg1, arg2, arg3);
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		Invokable fn = callTarget(state, target);
		if (fn == target) fn.invoke(state, result, arg1, arg2, arg3, arg4);
		else fn.invoke(state, result, target, arg1, arg2, arg3, arg4);
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		Invokable fn = callTarget(state, target);
		if (fn == target) fn.invoke(state, result, arg1, arg2, arg3, arg4, arg5);
		else fn.invoke(state, result, new Object[] { target, arg1, arg2, arg3, arg4, arg5 });
	}

	public static void mt_invoke(LuaState state, ObjectSink result, Object target, Object[] args) throws ControlThrowable {
		Invokable fn = callTarget(state, target);
		if (fn == target) {
			fn.invoke(state, result, args);
		}
		else {
			Object[] mtArgs = new Object[args.length + 1];
			mtArgs[0] = target;
			System.arraycopy(args, 0, mtArgs, 1, args.length);
			fn.invoke(state, result, mtArgs);
		}
	}

	public static void evaluateTailCalls(LuaState state, ObjectSink r) throws ControlThrowable {
		while (r.isTailCall()) {
			switch (r.size()) {
				case 0: throw new IllegalStateException();
				case 1: mt_invoke(state, r, r._0()); break;
				case 2: mt_invoke(state, r, r._0(), r._1()); break;
				case 3: mt_invoke(state, r, r._0(), r._1(), r._2()); break;
				case 4: mt_invoke(state, r, r._0(), r._1(), r._2(), r._3()); break;
				case 5: mt_invoke(state, r, r._0(), r._1(), r._2(), r._3(), r._4()); break;
				default: mt_invoke(state, r, r._0(), r.tailAsArray()); break;
			}
		}
	}

	public static void call(LuaState state, ObjectSink result, Object target) throws ControlThrowable {
		mt_invoke(state, result, target);
		evaluateTailCalls(state, result);
	}

	public static void call(LuaState state, ObjectSink result, Object target, Object arg1) throws ControlThrowable {
		mt_invoke(state, result, target, arg1);
		evaluateTailCalls(state, result);
	}

	public static void call(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2) throws ControlThrowable {
		mt_invoke(state, result, target, arg1, arg2);
		evaluateTailCalls(state, result);
	}

	public static void call(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2, Object arg3) throws ControlThrowable {
		mt_invoke(state, result, target, arg1, arg2, arg3);
		evaluateTailCalls(state, result);
	}

	public static void call(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2, Object arg3, Object arg4) throws ControlThrowable {
		mt_invoke(state, result, target, arg1, arg2, arg3, arg4);
		evaluateTailCalls(state, result);
	}

	public static void call(LuaState state, ObjectSink result, Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws ControlThrowable {
		mt_invoke(state, result, target, arg1, arg2, arg3, arg4, arg5);
		evaluateTailCalls(state, result);
	}

	public static void call(LuaState state, ObjectSink result, Object target, Object[] args) throws ControlThrowable {
		mt_invoke(state, result, target, args);
		evaluateTailCalls(state, result);
	}

	private static void try_mt_arithmetic(LuaState state, ObjectSink result, String event, Object a, Object b) throws ControlThrowable {
		Object handler = Metatables.binaryHandlerFor(state, event, a, b);

		if (handler != null) {
			call(state, result, handler, a, b);
		}
		else {
			throw IllegalOperationAttemptException.arithmetic(a, b);
		}
	}

	private static void try_mt_arithmetic(LuaState state, ObjectSink result, String event, Object o) throws ControlThrowable {
		Object handler = Metatables.getMetamethod(state, event, o);

		if (handler != null) {
			call(state, result, handler, o);
		}
		else {
			throw IllegalOperationAttemptException.arithmetic(o);
		}
	}

	public static void add(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		MathImplementation math = MathImplementation.arithmetic(a, b);
		if (math != null) {
			result.setTo(math.do_add(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(state, result, Metatables.MT_ADD, a, b);
		}
	}

	public static Number add(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_add(a, b);
	}

	public static void sub(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(a, b);
		if (m != null) {
			result.setTo(m.do_sub(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(state, result, Metatables.MT_SUB, a, b);
		}
	}

	public static Number sub(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_sub(a, b);
	}

	public static void mul(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(a, b);
		if (m != null) {
			result.setTo(m.do_mul(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(state, result, Metatables.MT_MUL, a, b);
		}
	}

	public static Number mul(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_mul(a, b);
	}

	public static void div(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(a, b);
		if (m != null) {
			result.setTo(m.do_div(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(state, result, Metatables.MT_DIV, a, b);
		}
	}

	public static Number div(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_div(a, b);
	}

	public static void mod(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(a, b);
		if (m != null) {
			result.setTo(m.do_mod(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(state, result, Metatables.MT_MOD, a, b);
		}
	}

	public static Number mod(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_mod(a, b);
	}

	public static void idiv(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(a, b);
		if (m != null) {
			result.setTo(m.do_idiv(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(state, result, Metatables.MT_IDIV, a, b);
		}
	}

	public static Number idiv(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_idiv(a, b);
	}

	public static void pow(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(a, b);
		if (m != null) {
			result.setTo(m.do_pow(Conversions.objectAsNumber(a), Conversions.objectAsNumber(b)));
		}
		else {
			try_mt_arithmetic(state, result, Metatables.MT_POW, a, b);
		}
	}

	public static Number pow(Number a, Number b) {
		return MathImplementation.arithmetic(a, b).do_pow(a, b);
	}

	private static void try_mt_bitwise(LuaState state, ObjectSink result, String event, Object a, Object b) throws ControlThrowable {
		Object handler = Metatables.binaryHandlerFor(state, event, a, b);

		if (handler != null) {
			call(state, result, handler, a, b);
		}
		else {
			throw IllegalOperationAttemptException.bitwise(a, b);
		}
	}

	private static void try_mt_bitwise(LuaState state, ObjectSink result, String event, Object o) throws ControlThrowable {
		Object handler = Metatables.getMetamethod(state, event, o);

		if (handler != null) {
			call(state, result, handler, o);
		}
		else {
			throw IllegalOperationAttemptException.bitwise(o);
		}
	}

	public static void band(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);

		if (la != null && lb != null) {
			result.setTo(la & lb);
		}
		else {
			try_mt_bitwise(state, result, Metatables.MT_BAND, a, b);
		}
	}

	public static void bor(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);

		if (la != null && lb != null) {
			result.setTo(la | lb);
		}
		else {
			try_mt_bitwise(state, result, Metatables.MT_BOR, a, b);
		}
	}

	public static void bxor(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);

		if (la != null && lb != null) {
			result.setTo(la ^ lb);
		}
		else {
			try_mt_bitwise(state, result, Metatables.MT_BXOR, a, b);
		}
	}

	public static void shl(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);

		if (la != null && lb != null) {
			result.setTo(la << lb);
		}
		else {
			try_mt_bitwise(state, result, Metatables.MT_SHL, a, b);
		}
	}

	public static void shr(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);

		if (la != null && lb != null) {
			result.setTo(la >>> lb);
		}
		else {
			try_mt_bitwise(state, result, Metatables.MT_SHR, a, b);
		}
	}

	public static void unm(LuaState state, ObjectSink result, Object o) throws ControlThrowable {
		MathImplementation m = MathImplementation.arithmetic(o);

		if (m != null) {
			result.setTo(m.do_unm(Conversions.objectAsNumber(o)));
		}
		else {
			try_mt_arithmetic(state, result, Metatables.MT_UNM, o);
		}
	}

	public static Number unm(Number n) {
		return MathImplementation.arithmetic(n).do_unm(n);
	}

	public static void bnot(LuaState state, ObjectSink result, Object o) throws ControlThrowable {
		Long lo = Conversions.objectAsLong(o);

		if (lo != null) {
			result.setTo(~lo);
		}
		else {
			try_mt_bitwise(state, result, Metatables.MT_BNOT, o);
		}
	}

	public static void len(LuaState state, ObjectSink result, Object o) throws ControlThrowable {
		if (o instanceof String) {
			result.setTo((long) RawOperators.stringLen((String) o));
		}
		else {
			Object handler = Metatables.getMetamethod(state, Metatables.MT_LEN, o);
			if (handler != null) {
				call(state, result, handler, o);
			}
			else if (o instanceof Table) {
				result.setTo((long) ((Table) o).rawlen());
			}
			else {
				throw IllegalOperationAttemptException.length(o);
			}
		}
	}

	public static void concat(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		String sa = Conversions.objectAsString(a);
		String sb = Conversions.objectAsString(b);

		if (sa != null && sb != null) {
			result.setTo(sa.concat(sb));
		}
		else {
			Object handler = Metatables.binaryHandlerFor(state, Metatables.MT_CONCAT, a, b);
			if (handler != null) {
				call(state, result, handler, a, b);
			}
			else {
				throw IllegalOperationAttemptException.concatenate(a, b);
			}
		}
	}

	private static class ComparisonResumable implements Resumable {

		@Override
		public void resume(LuaState state, ObjectSink result, Serializable suspendedState) throws ControlThrowable {
			Boolean b = (Boolean) suspendedState;
			boolean resultValue = Conversions.objectToBoolean(result._0());
			result.setTo(b == resultValue);
		}

	}

	private static void _call_comparison_mt(LuaState state, ObjectSink result, boolean cmpTo, Object handler, Object a, Object b) throws ControlThrowable {
		try {
			call(state, result, handler, a, b);
		}
		catch (ControlThrowable ct) {
			// suspended in the metamethod call
			ct.push(new ComparisonResumable(), cmpTo);
			throw ct;
		}
		// not suspended: set the result, possibly flipping it
		result.setTo(Conversions.objectToBoolean(result._0()) == cmpTo);
	}

	public static void eq(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		boolean rawEqual = RawOperators.raweq(a, b);

		if (!rawEqual
				&& ((a instanceof Table && b instanceof Table)
				|| (a instanceof Userdata && b instanceof Userdata))) {

			Object handler = Metatables.binaryHandlerFor(state, Metatables.MT_EQ, a, b);

			if (handler != null) {
				_call_comparison_mt(state, result, true, handler, a, b);
				return;
			}

			// else keep the result as false
		}

		result.setTo(rawEqual);
	}

	public static void lt(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		ComparisonImplementation c = ComparisonImplementation.of(a, b);
		if (c != null) {
			result.setTo(c.do_lt(a, b));
		}
		else {
			Object handler = Metatables.binaryHandlerFor(state, Metatables.MT_LT, a, b);

			if (handler != null) {
				_call_comparison_mt(state, result, true, handler, a, b);
			}
			else {
				throw IllegalOperationAttemptException.comparison(a, b);
			}
		}
	}

	public static void le(LuaState state, ObjectSink result, Object a, Object b) throws ControlThrowable {
		ComparisonImplementation c = ComparisonImplementation.of(a, b);
		if (c != null) {
			result.setTo(c.do_le(a, b));
		}
		else {
			Object le_handler = Metatables.binaryHandlerFor(state, Metatables.MT_LE, a, b);

			if (le_handler != null) {
				_call_comparison_mt(state, result, true, le_handler, a, b);
			}
			else {
				// TODO: verify that (a, b) is the order in which the metamethod is looked up
				Object lt_handler = Metatables.binaryHandlerFor(state, Metatables.MT_LT, a, b);

				if (lt_handler != null) {
					// will be evaluating "not (b < a)"
					_call_comparison_mt(state, result, false, lt_handler, b, a);
				}
				else {
					throw IllegalOperationAttemptException.comparison(a, b);
				}
			}
		}
	}

	public static void index(LuaState state, ObjectSink result, Object table, Object key) throws ControlThrowable {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object value = t.rawget(key);

			if (value != null) {
				result.setTo(value);
				return;
			}
		}

		Object handler = Metatables.getMetamethod(state, Metatables.MT_INDEX, table);

		if (handler == null && table instanceof Table) {
			// key not found and no index metamethod, returning nil
			result.setTo(null);
			return;
		}
		if (handler instanceof Invokable) {
			// call the handler
			Invokable fn = (Invokable) handler;

			fn.invoke(state, result, handler, table, key);
			evaluateTailCalls(state, result);
		}
		else if (handler instanceof Table) {
			// TODO: protect against infinite loops
			index(state, result, handler, key);
		}
		else {
			throw IllegalOperationAttemptException.index(table);
		}
	}

	public static void newindex(LuaState state, ObjectSink result, Object table, Object key, Object value) throws ControlThrowable {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object r = t.rawget(key);

			if (result != null) {
				t.rawset(key, value);
				return;
			}
		}

		Object handler = Metatables.getMetamethod(state, Metatables.MT_NEWINDEX, table);

		if (handler == null && table instanceof Table) {
			Table t = (Table) table;
			t.rawset(key, value);
			return;
		}

		if (handler instanceof Invokable) {
			// call the handler
			Invokable fn = (Invokable) handler;

			fn.invoke(state, result, handler, table, key, value);
			evaluateTailCalls(state, result);
		}
		else if (handler instanceof Table) {
			// TODO: protect against infinite loops
			newindex(state, result, handler, key, value);
		}
		else {
			throw IllegalOperationAttemptException.index(table);
		}
	}

	private static boolean isNonZero(MathImplementation m, Number n) {
		return !m.do_eq(0L, n);
	}

	public static boolean continueLoop(Number index, Number limit, Number step) {
		MathImplementation m_step = MathImplementation.arithmetic(step, 0L);
		if (!isNonZero(m_step, step)) {
			return false;  // step is zero or NaN
		}

		boolean ascending = m_step.do_lt(0L, step);

		MathImplementation m_cmp = MathImplementation.arithmetic(index, limit);
		return ascending
				? m_cmp.do_le(index, limit)
				: m_cmp.do_le(limit, index);
	}

}

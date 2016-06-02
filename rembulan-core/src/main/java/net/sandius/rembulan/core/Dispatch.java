package net.sandius.rembulan.core;

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

	public static Preemption mt_invoke(ExecutionContext context, Object target) {
		Invokable fn = callTarget(context.getState(), target);
		return fn == target
				? fn.invoke(context)
				: fn.invoke(context, target);
	}

	public static Preemption mt_invoke(ExecutionContext context, Object target, Object arg1) {
		Invokable fn = callTarget(context.getState(), target);
		return fn == target
				? fn.invoke(context, arg1)
				: fn.invoke(context, target, arg1);
	}

	public static Preemption mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2) {
		Invokable fn = callTarget(context.getState(), target);
		return fn == target
				? fn.invoke(context, arg1, arg2)
				: fn.invoke(context, target, arg1, arg2);
	}

	public static Preemption mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3) {
		Invokable fn = callTarget(context.getState(), target);
		return fn == target
				? fn.invoke(context, arg1, arg2, arg3)
				: fn.invoke(context, target, arg1, arg2, arg3);
	}

	public static Preemption mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4) {
		Invokable fn = callTarget(context.getState(), target);
		return fn == target
				? fn.invoke(context, arg1, arg2, arg3, arg4)
				: fn.invoke(context, target, arg1, arg2, arg3, arg4);
	}

	public static Preemption mt_invoke(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		Invokable fn = callTarget(context.getState(), target);
		return fn == target
				? fn.invoke(context, arg1, arg2, arg3, arg4, arg5)
				: fn.invoke(context, new Object[] { target, arg1, arg2, arg3, arg4, arg5 });
	}

	public static Preemption mt_invoke(ExecutionContext context, Object target, Object[] args) {
		Invokable fn = callTarget(context.getState(), target);
		if (fn == target) {
			return fn.invoke(context, args);
		}
		else {
			Object[] mtArgs = new Object[args.length + 1];
			mtArgs[0] = target;
			System.arraycopy(args, 0, mtArgs, 1, args.length);
			return fn.invoke(context, mtArgs);
		}
	}

	public static Preemption evaluateTailCalls(ExecutionContext context) {
		ObjectSink r = context.getObjectSink();
		while (r.isTailCall()) {
			Object target = r.getTailCallTarget();
			final Preemption p;
			switch (r.size()) {
				case 0: p = mt_invoke(context, target); break;
				case 1: p = mt_invoke(context, target, r._0()); break;
				case 2: p = mt_invoke(context, target, r._0(), r._1()); break;
				case 3: p = mt_invoke(context, target, r._0(), r._1(), r._2()); break;
				case 4: p = mt_invoke(context, target, r._0(), r._1(), r._2(), r._3()); break;
				case 5: p = mt_invoke(context, target, r._0(), r._1(), r._2(), r._3(), r._4()); break;
				default: p = mt_invoke(context, target, r.toArray()); break;
			}
			if (p != null) {
				return p;
			}
		}
		return null;
	}

	public static Preemption call(ExecutionContext context, Object target) {
		Preemption p = mt_invoke(context, target);
		return p == null ? evaluateTailCalls(context) : p;
	}

	public static Preemption call(ExecutionContext context, Object target, Object arg1) {
		Preemption p = mt_invoke(context, target, arg1);
		return p == null ? evaluateTailCalls(context) : p;
	}

	public static Preemption call(ExecutionContext context, Object target, Object arg1, Object arg2) {
		Preemption p = mt_invoke(context, target, arg1, arg2);
		return p == null ? evaluateTailCalls(context) : p;
	}

	public static Preemption call(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3) {
		Preemption p = mt_invoke(context, target, arg1, arg2, arg3);
		return p == null ? evaluateTailCalls(context) : p;
	}

	public static Preemption call(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4) {
		Preemption p = mt_invoke(context, target, arg1, arg2, arg3, arg4);
		return p == null ? evaluateTailCalls(context) : p;
	}

	public static Preemption call(ExecutionContext context, Object target, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		Preemption p = mt_invoke(context, target, arg1, arg2, arg3, arg4, arg5);
		return p == null ? evaluateTailCalls(context) : p;
	}

	public static Preemption call(ExecutionContext context, Object target, Object[] args) {
		Preemption p = mt_invoke(context, target, args);
		return p == null ? evaluateTailCalls(context) : p;
	}

	private static Preemption try_mt_arithmetic(ExecutionContext context, String event, Object a, Object b) {
		Object handler = Metatables.binaryHandlerFor(context.getState(), event, a, b);

		if (handler != null) {
			return call(context, handler, a, b);
		}
		else {
			throw IllegalOperationAttemptException.arithmetic(a, b);
		}
	}

	private static Preemption try_mt_arithmetic(ExecutionContext context, String event, Object o) {
		Object handler = Metatables.getMetamethod(context.getState(), event, o);

		if (handler != null) {
			return call(context, handler, o);
		}
		else {
			throw IllegalOperationAttemptException.arithmetic(o);
		}
	}

	public static Preemption add(ExecutionContext context, Object a, Object b) {
		LNumber na = Conversions.objectAsFloatIfString(a);
		LNumber nb = Conversions.objectAsFloatIfString(b);

		if (na != null && nb != null) {
			context.getObjectSink().setTo(na.add(nb));
			return null;
		}
		else {
			return try_mt_arithmetic(context, Metatables.MT_ADD, a, b);
		}
	}

	public static LNumber add(LNumber a, LNumber b) {
		return a.add(b);
	}

	public static Preemption sub(ExecutionContext context, Object a, Object b) {
		LNumber na = Conversions.objectAsFloatIfString(a);
		LNumber nb = Conversions.objectAsFloatIfString(b);

		if (na != null && nb != null) {
			context.getObjectSink().setTo(na.sub(nb));
			return null;
		}
		else {
			return try_mt_arithmetic(context, Metatables.MT_SUB, a, b);
		}
	}

	public static LNumber sub(LNumber a, LNumber b) {
		return a.sub(b);
	}

	public static Preemption mul(ExecutionContext context, Object a, Object b) {
		LNumber na = Conversions.objectAsFloatIfString(a);
		LNumber nb = Conversions.objectAsFloatIfString(b);

		if (na != null && nb != null) {
			context.getObjectSink().setTo(na.mul(nb));
			return null;
		}
		else {
			return try_mt_arithmetic(context, Metatables.MT_MUL, a, b);
		}
	}

	public static LNumber mul(LNumber a, LNumber b) {
		return a.mul(b);
	}

	public static Preemption div(ExecutionContext context, Object a, Object b) {
		LNumber na = Conversions.objectAsFloatIfString(a);
		LNumber nb = Conversions.objectAsFloatIfString(b);

		if (na != null && nb != null) {
			context.getObjectSink().setTo(na.div(nb));
			return null;
		}
		else {
			return try_mt_arithmetic(context, Metatables.MT_DIV, a, b);
		}
	}

	public static LNumber div(LNumber a, LNumber b) {
		return a.div(b);
	}

	public static Preemption mod(ExecutionContext context, Object a, Object b) {
		LNumber na = Conversions.objectAsFloatIfString(a);
		LNumber nb = Conversions.objectAsFloatIfString(b);

		if (na != null && nb != null) {
			context.getObjectSink().setTo(na.mod(nb));
			return null;
		}
		else {
			return try_mt_arithmetic(context, Metatables.MT_MOD, a, b);
		}
	}

	public static LNumber mod(LNumber a, LNumber b) {
		return a.mod(b);
	}

	public static Preemption idiv(ExecutionContext context, Object a, Object b) {
		LNumber na = Conversions.objectAsFloatIfString(a);
		LNumber nb = Conversions.objectAsFloatIfString(b);

		if (na != null && nb != null) {
			context.getObjectSink().setTo(na.idiv(nb));
			return null;
		}
		else {
			return try_mt_arithmetic(context, Metatables.MT_IDIV, a, b);
		}
	}

	public static LNumber idiv(LNumber a, LNumber b) {
		return a.idiv(b);
	}

	public static Preemption pow(ExecutionContext context, Object a, Object b) {
		LNumber na = Conversions.objectAsFloatIfString(a);
		LNumber nb = Conversions.objectAsFloatIfString(b);

		if (na != null && nb != null) {
			context.getObjectSink().setTo(na.pow(nb));
			return null;
		}
		else {
			return try_mt_arithmetic(context, Metatables.MT_POW, a, b);
		}
	}

	public static LNumber pow(LNumber a, LNumber b) {
		return a.pow(b);
	}

	private static Preemption try_mt_bitwise(ExecutionContext context, String event, Object a, Object b) {
		Object handler = Metatables.binaryHandlerFor(context.getState(), event, a, b);

		if (handler != null) {
			return call(context, handler, a, b);
		}
		else {
			throw IllegalOperationAttemptException.bitwise(a, b);
		}
	}

	private static Preemption try_mt_bitwise(ExecutionContext context, String event, Object o) {
		Object handler = Metatables.getMetamethod(context.getState(), event, o);

		if (handler != null) {
			return call(context, handler, o);
		}
		else {
			throw IllegalOperationAttemptException.bitwise(o);
		}
	}

	public static Preemption band(ExecutionContext context, Object a, Object b) {
		LNumber na = Conversions.objectAsLNumber(a);
		LNumber nb = Conversions.objectAsLNumber(b);

		if (na != null && nb != null) {
			context.getObjectSink().setTo(na.band(nb));
			return null;
		}
		else {
			return try_mt_bitwise(context, Metatables.MT_BAND, a, b);
		}
	}

	public static Preemption bor(ExecutionContext context, Object a, Object b) {
		LNumber na = Conversions.objectAsLNumber(a);
		LNumber nb = Conversions.objectAsLNumber(b);

		if (na != null && nb != null) {
			context.getObjectSink().setTo(na.bor(nb));
			return null;
		}
		else {
			return try_mt_bitwise(context, Metatables.MT_BOR, a, b);
		}
	}

	public static Preemption bxor(ExecutionContext context, Object a, Object b) {
		LNumber na = Conversions.objectAsLNumber(a);
		LNumber nb = Conversions.objectAsLNumber(b);

		if (na != null && nb != null) {
			context.getObjectSink().setTo(na.bxor(nb));
			return null;
		}
		else {
			return try_mt_bitwise(context, Metatables.MT_BXOR, a, b);
		}
	}

	public static Preemption shl(ExecutionContext context, Object a, Object b) {
		LNumber na = Conversions.objectAsLNumber(a);
		LNumber nb = Conversions.objectAsLNumber(b);

		if (na != null && nb != null) {
			context.getObjectSink().setTo(na.shl(nb));
			return null;
		}
		else {
			return try_mt_bitwise(context, Metatables.MT_SHL, a, b);
		}
	}

	public static Preemption shr(ExecutionContext context, Object a, Object b) {
		LNumber na = Conversions.objectAsLNumber(a);
		LNumber nb = Conversions.objectAsLNumber(b);

		if (na != null && nb != null) {
			context.getObjectSink().setTo(na.shr(nb));
			return null;
		}
		else {
			return try_mt_bitwise(context, Metatables.MT_SHR, a, b);
		}
	}

	public static Preemption unm(ExecutionContext context, Object o) {
		LNumber no = Conversions.objectAsFloatIfString(o);
		if (no != null) {
			context.getObjectSink().setTo(no.unm());
			return null;
		}
		else {
			return try_mt_arithmetic(context, Metatables.MT_UNM, o);
		}
	}

	public static LNumber unm(LNumber n) {
		return n.unm();
	}

	public static Preemption bnot(ExecutionContext context, Object o) {
		LNumber no = Conversions.objectAsLNumber(o);

		if (no != null) {
			context.getObjectSink().setTo(no.bnot());
			return null;
		}
		else {
			return try_mt_bitwise(context, Metatables.MT_BNOT, o);
		}
	}

	public static Preemption len(ExecutionContext context, Object o) {
		if (o instanceof String) {
			context.getObjectSink().setTo(LInteger.valueOf(RawOperators.stringLen((String) o)));
			return null;
		}
		else {
			Object handler = Metatables.getMetamethod(context.getState(), Metatables.MT_LEN, o);
			if (handler != null) {
				return call(context, handler, o);
			}
			else if (o instanceof Table) {
				context.getObjectSink().setTo(LInteger.valueOf(((Table) o).rawlen()));
				return null;
			}
			else {
				throw IllegalOperationAttemptException.length(o);
			}
		}
	}

	public static Preemption concat(ExecutionContext context, Object a, Object b) {
		String sa = Conversions.objectAsString(a);
		String sb = Conversions.objectAsString(b);

		if (sa != null && sb != null) {
			context.getObjectSink().setTo(sa.concat(sb));
			return null;
		}
		else {
			Object handler = Metatables.binaryHandlerFor(context.getState(), Metatables.MT_CONCAT, a, b);
			if (handler != null) {
				return call(context, handler, a, b);
			}
			else {
				throw IllegalOperationAttemptException.concatenate(a, b);
			}
		}
	}

	private static class ComparisonResumable implements Resumable {

		public static final ComparisonResumable INSTANCE = new ComparisonResumable();

		@Override
		public Preemption resume(ExecutionContext context, Object suspendedState) {
			Boolean b = (Boolean) suspendedState;
			ObjectSink result = context.getObjectSink();
			boolean resultValue = Conversions.objectToBoolean(result._0());
			result.setTo(b == resultValue);
			return null;
		}

	}

	private static Preemption _call_comparison_mt(ExecutionContext context, boolean cmpTo, Object handler, Object a, Object b) {
		Preemption p = call(context, handler, a, b);
		if (p != null) {
			// suspended in the metamethod call
			p.push(ComparisonResumable.INSTANCE, cmpTo);
			return p;
		}
		else {
			// not suspended: set the result, possibly flipping it
			ObjectSink result = context.getObjectSink();
			result.setTo(Conversions.objectToBoolean(result._0()) == cmpTo);
			return null;
		}
	}

	public static Preemption eq(ExecutionContext context, Object a, Object b) {
		boolean rawEqual = RawOperators.raweq(a, b);

		if (!rawEqual
				&& ((a instanceof Table && b instanceof Table)
				|| (a instanceof Userdata && b instanceof Userdata))) {

			Object handler = Metatables.binaryHandlerFor(context.getState(), Metatables.MT_EQ, a, b);

			if (handler != null) {
				return _call_comparison_mt(context, true, handler, a, b);
			}

			// else keep the result as false
		}

		context.getObjectSink().setTo(rawEqual);
		return null;
	}

	public static boolean eq(LNumber a, LNumber b) {
		return ComparisonImplementation.of(a, b).do_eq(a, b);
	}


	public static Preemption lt(ExecutionContext context, Object a, Object b) {
		ComparisonImplementation c = ComparisonImplementation.of(a, b);
		if (c != null) {
			context.getObjectSink().setTo(c.do_lt(a, b));
			return null;
		}
		else {
			Object handler = Metatables.binaryHandlerFor(context.getState(), Metatables.MT_LT, a, b);

			if (handler != null) {
				return _call_comparison_mt(context, true, handler, a, b);
			}
			else {
				throw IllegalOperationAttemptException.comparison(a, b);
			}
		}
	}

	public static boolean lt(LNumber a, LNumber b) {
		return ComparisonImplementation.of(a, b).do_lt(a, b);
	}

	public static Preemption le(ExecutionContext context, Object a, Object b) {
		ComparisonImplementation c = ComparisonImplementation.of(a, b);
		if (c != null) {
			context.getObjectSink().setTo(c.do_le(a, b));
			return null;
		}
		else {
			LuaState state = context.getState();
			Object le_handler = Metatables.binaryHandlerFor(state, Metatables.MT_LE, a, b);

			if (le_handler != null) {
				return _call_comparison_mt(context, true, le_handler, a, b);
			}
			else {
				// TODO: verify that (a, b) is the order in which the metamethod is looked up
				Object lt_handler = Metatables.binaryHandlerFor(state, Metatables.MT_LT, a, b);

				if (lt_handler != null) {
					// will be evaluating "not (b < a)"
					return _call_comparison_mt(context, false, lt_handler, b, a);
				}
				else {
					throw IllegalOperationAttemptException.comparison(a, b);
				}
			}
		}
	}

	public static boolean le(LNumber a, LNumber b) {
		return ComparisonImplementation.of(a, b).do_le(a, b);
	}

	public static Preemption index(ExecutionContext context, Object table, Object key) {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object value = t.rawget(key);

			if (value != null) {
				context.getObjectSink().setTo(value);
				return null;
			}
		}

		Object handler = Metatables.getMetamethod(context.getState(), Metatables.MT_INDEX, table);

		if (handler == null && table instanceof Table) {
			// key not found and no index metamethod, returning nil
			context.getObjectSink().setTo(null);
			return null;
		}
		if (handler instanceof Invokable) {
			// call the handler
			Invokable fn = (Invokable) handler;

			Preemption p = fn.invoke(context, handler, table, key);
			return p == null ? evaluateTailCalls(context) : p;
		}
		else if (handler instanceof Table) {
			// TODO: protect against infinite loops
			return index(context, handler, key);
		}
		else {
			throw IllegalOperationAttemptException.index(table);
		}
	}

	public static Preemption newindex(ExecutionContext context, Object table, Object key, Object value) {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object r = t.rawget(key);

			if (r != null) {
				t.rawset(key, value);
				return null;
			}
		}

		Object handler = Metatables.getMetamethod(context.getState(), Metatables.MT_NEWINDEX, table);

		if (handler == null && table instanceof Table) {
			Table t = (Table) table;
			t.rawset(key, value);
			return null;
		}

		if (handler instanceof Invokable) {
			// call the handler
			Invokable fn = (Invokable) handler;

			Preemption p = fn.invoke(context, handler, table, key, value);
			return p == null ? evaluateTailCalls(context) : p;
		}
		else if (handler instanceof Table) {
			// TODO: protect against infinite loops
			return newindex(context, handler, key, value);
		}
		else {
			throw IllegalOperationAttemptException.index(table);
		}
	}

	private static boolean isNonZero(LNumber n) {
		return !n.eq(LInteger.ZERO);
	}

	public static boolean continueLoop(LNumber index, LNumber limit, LNumber step) {
		if (!isNonZero(step)) {
			return false;  // step is zero or NaN
		}

		boolean ascending = LInteger.ZERO.lt(step);

		return ascending
				? index.le(limit)
				: limit.le(index);
	}

}

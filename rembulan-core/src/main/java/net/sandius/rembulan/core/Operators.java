package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

public class Operators {

	private static Object tryMetamethodCall(String event, Object a, Object b) {
		Check.notNull(event);

		Object handler = Metatables.binaryHandlerFor(event, a, b);
		if (handler == null) {
			throw new IllegalOperationAttemptException("perform operation on", "illegal");
		}
		else {
			return callHandler(handler, a, b);
		}
	}

	private static Object tryMetamethodCall(String event, Object o) {
		Check.notNull(event);

		Object handler = Metatables.getMetamethod(o, event);
		if (handler == null) {
			throw new IllegalOperationAttemptException("perform operation on", LuaType.typeOf(o).name);
		}
		else {
			return callHandler(handler, o);
		}
	}

	// trim to single value
	private static Object trim(Object o) {
		return o;  // TODO
	}

	private static Object callHandler(Object handler, Object[] args) {
		throw new UnsupportedOperationException();  // FIXME
	}

	private static Object callHandler(Object handler, Object o) {
		return callHandler(handler, new Object[]{o});
	}

	private static Object callHandler(Object handler, Object a, Object b) {
		return callHandler(handler, new Object[]{a, b});
	}

	private static Object callHandler(Object handler, Object a, Object[] b) {
		Check.notNull(b);

		Object[] args = new Object[b.length + 1];
		args[0] = a;
		System.arraycopy(b, 0, args, 1, b.length);

		return callHandler(handler, args);
	}

	private static Object callHandler(Object handler, Object a, Object b, Object c) {
		return callHandler(handler, new Object[]{a, b, c});
	}

	public static Object add(Object a, Object b) {
		Number na = Conversions.objectAsNumber(a);
		Number nb = Conversions.objectAsNumber(b);
		return na != null && nb != null ? RawOperators.rawadd(na, nb) : tryMetamethodCall(Metatables.MT_ADD, a, b);
	}

	public static Object sub(Object a, Object b) {
		Number na = Conversions.objectAsNumber(a);
		Number nb = Conversions.objectAsNumber(b);
		return na != null && nb != null ? RawOperators.rawsub(na, nb) : tryMetamethodCall(Metatables.MT_SUB, a, b);
	}

	public static Object mul(Object a, Object b) {
		Number na = Conversions.objectAsNumber(a);
		Number nb = Conversions.objectAsNumber(b);
		return na != null && nb != null ? RawOperators.rawmul(na, nb) : tryMetamethodCall(Metatables.MT_MUL, a, b);
	}

	public static Object div(Object a, Object b) {
		Number na = Conversions.objectAsNumber(a);
		Number nb = Conversions.objectAsNumber(b);
		return na != null && nb != null ? RawOperators.rawdiv(na, nb) : tryMetamethodCall(Metatables.MT_DIV, a, b);
	}

	public static Object mod(Object a, Object b) {
		Number na = Conversions.objectAsNumber(a);
		Number nb = Conversions.objectAsNumber(b);
		return na != null && nb != null ? RawOperators.rawmod(na, nb) : tryMetamethodCall(Metatables.MT_MOD, a, b);
	}

	public static Object pow(Object a, Object b) {
		Number na = Conversions.objectAsNumber(a);
		Number nb = Conversions.objectAsNumber(b);
		return na != null && nb != null ? RawOperators.rawpow(na, nb) : tryMetamethodCall(Metatables.MT_POW, a, b);
	}

	public static Object unm(Object o) {
		Number no = Conversions.objectAsNumber(o);
		return no != null ? RawOperators.rawunm(no) : tryMetamethodCall(Metatables.MT_UNM, o);
	}

	public static Object idiv(Object a, Object b) {
		Number na = Conversions.objectAsNumber(a);
		Number nb = Conversions.objectAsNumber(b);
		return na != null && nb != null ? RawOperators.rawidiv(na, nb) : tryMetamethodCall(Metatables.MT_IDIV, a, b);
	}

	public static Object band(Object a, Object b) {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);
		return la != null && lb != null ? RawOperators.rawband(la, lb) : tryMetamethodCall(Metatables.MT_BAND, a, b);
	}

	public static Object bor(Object a, Object b) {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);
		return la != null && lb != null ? RawOperators.rawbor(la, lb) : tryMetamethodCall(Metatables.MT_BOR, a, b);
	}

	public static Object bxor(Object a, Object b) {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);
		return la != null && lb != null ? RawOperators.rawbxor(la, lb) : tryMetamethodCall(Metatables.MT_BXOR, a, b);
	}

	public static Object bnot(Object o) {
		Long lo = Conversions.objectAsLong(o);
		return lo != null ? RawOperators.rawbnot(lo) : tryMetamethodCall(Metatables.MT_BNOT, o);
	}

	public static Object shl(Object a, Object b) {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);
		return la != null && lb != null ? RawOperators.rawshl(la, lb) : tryMetamethodCall(Metatables.MT_SHL, a, b);
	}

	public static Object shr(Object a, Object b) {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);
		return la != null && lb != null ? RawOperators.rawshr(la, lb) : tryMetamethodCall(Metatables.MT_SHR, a, b);
	}

	public static Object concat(Object a, Object b) {
		String sa = Conversions.objectAsString(a);
		String sb = Conversions.objectAsString(b);

		if (sa != null && sb != null) {
			return sa.concat(sb);
		}
		else {
			return tryMetamethodCall(Metatables.MT_CONCAT, a, b);
		}
	}

	public static Object len(Object o) {
		if (o != null && o instanceof String) {
			return RawOperators.stringLen((String) o);
		}
		else {
			Object handler = Metatables.getMetamethod(o, Metatables.MT_LEN);
			if (handler != null) {
				return callHandler(handler, o);
			}
			else {
				if (o != null && o instanceof Table) {
					return ((Table) o).rawlen();
				}
				else {
					throw new IllegalOperationAttemptException("get length of", LuaType.typeOf(o).name);
				}
			}
		}
	}

	public static boolean eq(Object a, Object b) {
		boolean result = RawOperators.raweq(a, b);

		if (!result &&
				((a instanceof Table && b instanceof Table)
				|| (a instanceof Userdata && b instanceof Userdata))
				|| (Value.isLightUserdata(a) && Value.isLightUserdata(b))) {

			return Conversions.objectToBoolean(tryMetamethodCall(Metatables.MT_EQ, a, b));
		}
		else {
			return result;
		}
	}

	public static boolean neq(Object a, Object b) {
		return !eq(a, b);
	}

	public static boolean lt(Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			return RawOperators.rawlt((Number) a, (Number) b);
		}
		else if (a instanceof String && b instanceof String) {
			return RawOperators.rawlt((String) a, (String) b);
		}
		else {
			return Conversions.objectToBoolean(tryMetamethodCall(Metatables.MT_LT, a, b));
		}
	}

	public static boolean le(Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			return RawOperators.rawle((Number) a, (Number) b);
		}
		else if (a instanceof String && b instanceof String) {
			return RawOperators.rawle((String) a, (String) b);
		}
		else {
			boolean flip = false;
			Object handler = Metatables.binaryHandlerFor(Metatables.MT_LE, a, b);

			if (handler == null) {
				handler = Metatables.binaryHandlerFor(Metatables.MT_LT, a, b);
				flip = true;  // we'll be evaluating "not (b > a)"
			}

			if (handler != null) {
				if (flip) {
					// swap arguments
					Object tmp = b;
					b = a;
					a = tmp;
				}

				boolean result = Conversions.objectToBoolean(callHandler(handler, a, b));

				return flip ? !result : result;
			}
			else {
				throw new IllegalOperationAttemptException("compare " + LuaType.typeOf(a) + " with " + LuaType.typeOf(b));
			}
		}
	}

	public static boolean gt(Object a, Object b) {
		return lt(b, a);
	}

	public static boolean ge(Object a, Object b) {
		return le(b, a);
	}

	public static Object and(Object a, Object b) {
		return Conversions.objectToBoolean(a) ? b : a;
	}

	public static Object or(Object a, Object b) {
		return Conversions.objectToBoolean(a) ? a : b;
	}

	public static boolean not(Object o) {
		return !Conversions.objectToBoolean(o);
	}

	public static Object index(Object table, Object key) {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object result = t.rawget(key);

			if (result != null) {
				return result;
			}
		}

		Object handler = Metatables.getMetamethod(table, Metatables.MT_INDEX);

		if (handler == null && table instanceof Table) {
			return null;
		}

		if (handler instanceof Invokable) {
			return callHandler(handler, table, key);  // TODO: should we trim to single value?
		}
		else if (handler instanceof Table) {
			return index(handler, key);  // TODO: protect against infinite loops
		}
		else {
			throw new IllegalOperationAttemptException("index", LuaType.typeOf(table).name);
		}
	}

	// TODO: should we return values from here?
	public static void newindex(Object table, Object key, Object value) {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object result = t.rawget(key);

			if (result != null) {
				t.rawset(key, value);
				return;
			}
		}

		Object handler = Metatables.getMetamethod(table, Metatables.MT_NEWINDEX);

		if (handler == null && table instanceof Table) {
			Table t = (Table) table;
			t.rawset(key, value);
			return;
		}

		if (handler instanceof Invokable) {
			callHandler(handler, table, key, value);  // TODO: is it ok that we're ignoring the result?
		}
		else if (handler instanceof Table) {
			newindex(handler, key, value);  // TODO: protect against infinite loops
		}
		else {
			throw new IllegalOperationAttemptException("index", LuaType.typeOf(table).name);
		}
	}

}

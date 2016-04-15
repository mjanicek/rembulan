package net.sandius.rembulan.core;

import net.sandius.rembulan.util.Check;

@Deprecated
public class Operators {

	public static Object tryMetamethodCall(LuaState state, String event, Object a, Object b) {
		Check.notNull(event);

		Object handler = Metatables.binaryHandlerFor(state, event, a, b);
		if (handler == null) {
			throw new IllegalOperationAttemptException("perform operation on", "illegal");
		}
		else {
			return callHandler(handler, a, b);
		}
	}

	public static Object tryMetamethodCall(LuaState state, String event, Object o) {
		Check.notNull(event);

		Object handler = Metatables.getMetamethod(state, event, o);
		if (handler == null) {
			throw new IllegalOperationAttemptException("perform operation on", Value.typeOf(o).name);
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

	public static Object add(LuaState state, Object a, Object b) {
		if (Value.isInteger(a)) {
			if (Value.isInteger(b)) {
				return Value.toInteger(a) + Value.toInteger(b);
			}
			else if (Value.isFloat(b)) {
				return Value.toInteger(a) + Value.toFloat(b);
			}
			else if (b instanceof String) {
				Double db = Conversions.stringAsDouble((String) b);
				if (db != null) {
					return Value.toInteger(a) + db;
				}
			}
		}
		else if (Value.isFloat(a)) {
			if (Value.isInteger(b)) {
				return Value.toFloat(a) + Value.toInteger(b);
			}
			else if (Value.isFloat(b)) {
				return Value.toFloat(a) + Value.toFloat(b);
			}
			else if (b instanceof String) {
				Double db = Conversions.stringAsDouble((String) b);
				if (db != null) {
					return Value.toFloat(a) + db;
				}
			}
		}
		else if (a instanceof String) {
			Double da = Conversions.stringAsDouble((String) a);
			if (da != null) {
				if (Value.isInteger(b)) {
					return da + Value.toInteger(b);
				}
				else if (Value.isFloat(b)) {
					return da + Value.toFloat(b);
				}
				else if (b instanceof String) {
					Double db = Conversions.stringAsDouble((String) b);
					if (db != null) {
						return da + db;
					}
				}
			}
		}

		return tryMetamethodCall(state, Metatables.MT_ADD, a, b);
	}

	public static Object sub(LuaState state, Object a, Object b) {
		if (Value.isInteger(a)) {
			if (Value.isInteger(b)) {
				return Value.toInteger(a) - Value.toInteger(b);
			}
			else if (Value.isFloat(b)) {
				return Value.toInteger(a) - Value.toFloat(b);
			}
			else if (b instanceof String) {
				Double db = Conversions.stringAsDouble((String) b);
				if (db != null) {
					return Value.toInteger(a) - db;
				}
			}
		}
		else if (Value.isFloat(a)) {
			if (Value.isInteger(b)) {
				return Value.toFloat(a) - Value.toInteger(b);
			}
			else if (Value.isFloat(b)) {
				return Value.toFloat(a) - Value.toFloat(b);
			}
			else if (b instanceof String) {
				Double db = Conversions.stringAsDouble((String) b);
				if (db != null) {
					return Value.toFloat(a) - db;
				}
			}
		}
		else if (a instanceof String) {
			Double da = Conversions.stringAsDouble((String) a);
			if (da != null) {
				if (Value.isInteger(b)) {
					return da - Value.toInteger(b);
				}
				else if (Value.isFloat(b)) {
					return da - Value.toFloat(b);
				}
				else if (b instanceof String) {
					Double db = Conversions.stringAsDouble((String) b);
					if (db != null) {
						return da - db;
					}
				}
			}
		}

		return tryMetamethodCall(state, Metatables.MT_SUB, a, b);
	}

	public static Object mul(LuaState state, Object a, Object b) {
		throw new UnsupportedOperationException();  // TODO
//		Number na = Conversions.objectAsNumber(a);
//		Number nb = Conversions.objectAsNumber(b);
//		return na != null && nb != null ? RawOperators.rawmul(na, nb) : tryMetamethodCall(state, Metatables.MT_MUL, a, b);
	}

	public static Object div(LuaState state, Object a, Object b) {
		throw new UnsupportedOperationException();  // TODO
//		Number na = Conversions.objectAsNumber(a);
//		Number nb = Conversions.objectAsNumber(b);
//		return na != null && nb != null ? RawOperators.rawdiv(na, nb) : tryMetamethodCall(state, Metatables.MT_DIV, a, b);
	}

	public static Object mod(LuaState state, Object a, Object b) {
		throw new UnsupportedOperationException();  // TODO
//		Number na = Conversions.objectAsNumber(a);
//		Number nb = Conversions.objectAsNumber(b);
//		return na != null && nb != null ? RawOperators.rawmod(na, nb) : tryMetamethodCall(state, Metatables.MT_MOD, a, b);
	}

	public static Object pow(LuaState state, Object a, Object b) {
		throw new UnsupportedOperationException();  // TODO
//		Number na = Conversions.objectAsNumber(a);
//		Number nb = Conversions.objectAsNumber(b);
//		return na != null && nb != null ? RawOperators.rawpow(na, nb) : tryMetamethodCall(state, Metatables.MT_POW, a, b);
	}

	public static Object unm(LuaState state, Object o) {
		throw new UnsupportedOperationException();  // TODO
//		Number no = Conversions.objectAsNumber(o);
//		return no != null ? RawOperators.rawunm(no) : tryMetamethodCall(state, Metatables.MT_UNM, o);
	}

	public static Object idiv(LuaState state, Object a, Object b) {
		throw new UnsupportedOperationException();  // TODO
//		Number na = Conversions.objectAsNumber(a);
//		Number nb = Conversions.objectAsNumber(b);
//		return na != null && nb != null ? RawOperators.rawidiv(na, nb) : tryMetamethodCall(state, Metatables.MT_IDIV, a, b);
	}

	public static Object band(LuaState state, Object a, Object b) {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);
		return la != null && lb != null ? la & lb : tryMetamethodCall(state, Metatables.MT_BAND, a, b);
	}

	public static Object bor(LuaState state, Object a, Object b) {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);
		return la != null && lb != null ? la | lb : tryMetamethodCall(state, Metatables.MT_BOR, a, b);
	}

	public static Object bxor(LuaState state, Object a, Object b) {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);
		return la != null && lb != null ? la ^ lb : tryMetamethodCall(state, Metatables.MT_BXOR, a, b);
	}

	public static Object bnot(LuaState state, Object o) {
		Long lo = Conversions.objectAsLong(o);
		return lo != null ? ~lo : tryMetamethodCall(state, Metatables.MT_BNOT, o);
	}

	public static Object shl(LuaState state, Object a, Object b) {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);
		return la != null && lb != null ? la << lb : tryMetamethodCall(state, Metatables.MT_SHL, a, b);
	}

	public static Object shr(LuaState state, Object a, Object b) {
		Long la = Conversions.objectAsLong(a);
		Long lb = Conversions.objectAsLong(b);
		return la != null && lb != null ? la >>> lb : tryMetamethodCall(state, Metatables.MT_SHR, a, b);
	}

	public static Object concat(LuaState state, Object a, Object b) {
		String sa = Coercions.asString(a);
		String sb = Coercions.asString(b);

		if (sa != null && sb != null) {
			return sa.concat(sb);
		}
		else {
			return tryMetamethodCall(state, Metatables.MT_CONCAT, a, b);
		}
	}

	public static Object len(LuaState state, Object o) {
		if (o != null && o instanceof String) {
			return RawOperators.stringLen((String) o);
		}
		else {
			Object handler = Metatables.getMetamethod(state, Metatables.MT_LEN, o);
			if (handler != null) {
				return callHandler(handler, o);
			}
			else {
				if (o != null && o instanceof Table) {
					return ((Table) o).rawlen();
				}
				else {
					throw new IllegalOperationAttemptException("get length of", Value.typeOf(o).name);
				}
			}
		}
	}

	public static boolean eq(LuaState state, Object a, Object b) {
		boolean result = RawOperators.raweq(a, b);

		if (!result &&
				((a instanceof Table && b instanceof Table)
				|| (a instanceof Userdata && b instanceof Userdata))
				|| (Value.isLightUserdata(a) && Value.isLightUserdata(b))) {

			return Conversions.objectToBoolean(tryMetamethodCall(state, Metatables.MT_EQ, a, b));
		}
		else {
			return result;
		}
	}

	public static boolean neq(LuaState state, Object a, Object b) {
		return !eq(state, a, b);
	}

	public static boolean lt(LuaState state, Object a, Object b) {
		if (Value.isInteger(a)) {
			if (Value.isInteger(b)) {
				return RawOperators.rawlt(Value.toInteger(a), Value.toInteger(b));
			}
			else if (Value.isFloat(b)) {
				return RawOperators.rawlt(Value.toInteger(a), Value.toFloat(b));
			}
		}
		else if (Value.isFloat(a)) {
			if (Value.isInteger(b)) {
				return RawOperators.rawlt(Value.toFloat(a), Value.toInteger(b));
			}
			else if (Value.isFloat(b)) {
				return RawOperators.rawlt(Value.toFloat(a), Value.toFloat(b));
			}
		}
		else if (a instanceof String && b instanceof String) {
			return RawOperators.rawlt((String) a, (String) b);
		}

		return Conversions.objectToBoolean(tryMetamethodCall(state, Metatables.MT_LT, a, b));
	}

	public static boolean lt(LuaState state, long a, Object b) {
		if (Value.isInteger(b)) {
			return RawOperators.rawlt(Value.toInteger(a), Value.toInteger(b));
		}
		else if (Value.isFloat(b)) {
			return RawOperators.rawlt(Value.toInteger(a), Value.toFloat(b));
		}

		return Conversions.objectToBoolean(tryMetamethodCall(state, Metatables.MT_LT, a, b));
	}

	public static boolean le(LuaState state, Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			return RawOperators.rawle((Number) a, (Number) b);
		}
		else if (a instanceof String && b instanceof String) {
			return RawOperators.rawle((String) a, (String) b);
		}
		else {
			boolean flip = false;
			Object handler = Metatables.binaryHandlerFor(state, Metatables.MT_LE, a, b);

			if (handler == null) {
				handler = Metatables.binaryHandlerFor(state, Metatables.MT_LT, a, b);
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
				throw new IllegalOperationAttemptException("compare " + Value.typeOf(a) + " with " + Value.typeOf(b));
			}
		}
	}

	public static boolean gt(LuaState state, Object a, Object b) {
		return lt(state, b, a);
	}

	public static boolean ge(LuaState state, Object a, Object b) {
		return le(state, b, a);
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

	public static Object index(LuaState state, Object table, Object key) {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object result = t.rawget(key);

			if (result != null) {
				return result;
			}
		}

		Object handler = Metatables.getMetamethod(state, Metatables.MT_INDEX, table);

		if (handler == null && table instanceof Table) {
			return null;
		}

		if (handler instanceof Invokable) {
			return callHandler(handler, table, key);  // TODO: should we trim to single value?
		}
		else if (handler instanceof Table) {
			return index(state, handler, key);  // TODO: protect against infinite loops
		}
		else {
			throw new IllegalOperationAttemptException("index", Value.typeOf(table).name);
		}
	}

	// TODO: should we return values from here?
	public static void newindex(LuaState state, Object table, Object key, Object value) {
		if (table instanceof Table) {
			Table t = (Table) table;
			Object result = t.rawget(key);

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
			callHandler(handler, table, key, value);  // TODO: is it ok that we're ignoring the result?
		}
		else if (handler instanceof Table) {
			newindex(state, handler, key, value);  // TODO: protect against infinite loops
		}
		else {
			throw new IllegalOperationAttemptException("index", Value.typeOf(table).name);
		}
	}

}

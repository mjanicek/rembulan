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

import static net.sandius.rembulan.LuaMathOperators.rawadd;
import static net.sandius.rembulan.LuaMathOperators.rawdiv;
import static net.sandius.rembulan.LuaMathOperators.rawidiv;
import static net.sandius.rembulan.LuaMathOperators.rawmod;
import static net.sandius.rembulan.LuaMathOperators.rawmul;
import static net.sandius.rembulan.LuaMathOperators.rawpow;
import static net.sandius.rembulan.LuaMathOperators.rawsub;
import static net.sandius.rembulan.LuaMathOperators.rawunm;

public abstract class Arithmetic {

	private Arithmetic() {
		// not to be instantiated by the outside world
	}

	public static final IntegerArithmetic INTEGER = new IntegerArithmetic();
	public static final FloatArithmetic FLOAT = new FloatArithmetic();

	public abstract Number do_add(Number a, Number b);
	public abstract Number do_sub(Number a, Number b);
	public abstract Number do_mul(Number a, Number b);
	public abstract Double do_div(Number a, Number b);
	public abstract Number do_mod(Number a, Number b);
	public abstract Number do_idiv(Number a, Number b);
	public abstract Double do_pow(Number a, Number b);

	public abstract Number do_unm(Number n);

	public static final class IntegerArithmetic extends Arithmetic {

		@Override
		public Long do_add(Number a, Number b) {
			return rawadd(a.longValue(), b.longValue());
		}

		@Override
		public Long do_sub(Number a, Number b) {
			return rawsub(a.longValue(), b.longValue());
		}

		@Override
		public Long do_mul(Number a, Number b) {
			return rawmul(a.longValue(), b.longValue());
		}

		@Override
		public Double do_div(Number a, Number b) {
			return rawdiv(a.longValue(), b.longValue());
		}

		@Override
		public Long do_mod(Number a, Number b) {
			return rawmod(a.longValue(), b.longValue());
		}

		@Override
		public Long do_idiv(Number a, Number b) {
			return rawidiv(a.longValue(), b.longValue());
		}

		@Override
		public Double do_pow(Number a, Number b) {
			return rawpow(a.longValue(), b.longValue());
		}

		@Override
		public Long do_unm(Number n) {
			return rawunm(n.longValue());
		}

	}

	public static final class FloatArithmetic extends Arithmetic {

		@Override
		public Double do_add(Number a, Number b) {
			return rawadd(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_sub(Number a, Number b) {
			return rawsub(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_mul(Number a, Number b) {
			return rawmul(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_div(Number a, Number b) {
			return rawdiv(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_mod(Number a, Number b) {
			return rawmod(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_idiv(Number a, Number b) {
			return rawidiv(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_pow(Number a, Number b) {
			return rawpow(a.doubleValue(), b.doubleValue());
		}

		@Override
		public Double do_unm(Number n) {
			return rawunm(n.doubleValue());
		}

	}

	public static Arithmetic of(Object a, Object b) {
		return of(Conversions.arithmeticValueOf(a), Conversions.arithmeticValueOf(b));
	}

	public static Arithmetic of(Number a, Number b) {
		if (a == null || b == null) {
			return null;
		}
		else if ((a instanceof Double || a instanceof Float)
				|| (b instanceof Double || b instanceof Float)) {
			return FLOAT;
		}
		else {
			return INTEGER;
		}
	}

	public static Arithmetic of(Object o) {
		return of(Conversions.arithmeticValueOf(o));
	}

	public static Arithmetic of(Number n) {
		if (n == null) {
			return null;
		}
		else if (n instanceof Double || n instanceof Float) {
			return FLOAT;
		}
		else {
			return INTEGER;
		}
	}

}
